package com.contrabajo.servicios_api.service;

import com.contrabajo.servicios_api.dto.CitaServicioResponseDTO;
import com.contrabajo.servicios_api.dto.NotificacionWebSocketDTO;
import com.contrabajo.servicios_api.dto.SolicitarCitaDTO;
import com.contrabajo.servicios_api.model.CitaServicio;
import com.contrabajo.servicios_api.model.Estado;
import com.contrabajo.servicios_api.model.OfertaServicio;
import com.contrabajo.servicios_api.repository.CitaServicioRepository;
import com.contrabajo.servicios_api.repository.EstadoRepository;
import com.contrabajo.servicios_api.repository.OfertaServicioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CitaServicioService {

    private final CitaServicioRepository citaRepository;
    private final OfertaServicioRepository ofertaRepository;
    private final EstadoRepository estadoRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // ==========================================
    // 1. SOLICITAR SERVICIO (Inicio del flujo)
    // ==========================================
    @Transactional
    public CitaServicioResponseDTO solicitarServicio(SolicitarCitaDTO dto, Integer idCliente, Integer idCoordenadas) {
        OfertaServicio oferta = ofertaRepository.findById(dto.getIdOfertaServicio())
                .orElseThrow(() -> new RuntimeException("La oferta no existe."));

        if (!oferta.getDisponible() || oferta.getBorrado()) 
            throw new RuntimeException("Oferta no disponible.");

        Estado estadoPendiente = estadoRepository.findByCodigo("CITA_PENDIENTE")
                .orElseThrow(() -> new RuntimeException("Estado inicial no configurado."));

        CitaServicio nuevaCita = new CitaServicio();
        nuevaCita.setComentario(dto.getComentario());
        nuevaCita.setIdCoordenadas(idCoordenadas); 
        nuevaCita.setFechaSolicitud(LocalDateTime.now());
        nuevaCita.setOfertaServicio(oferta);
        nuevaCita.setCategoriaServicio(oferta.getCategoriaServicio());
        nuevaCita.setIdTrabajador(oferta.getIdTrabajador());
        nuevaCita.setIdCliente(idCliente);
        nuevaCita.setEstado(estadoPendiente);

        CitaServicio guardada = citaRepository.save(nuevaCita);

        // NOTIFICACIÓN: Se le avisa al TRABAJADOR que tiene una nueva solicitud
        enviarNotificacion("NUEVA_SOLICITUD", guardada.getId().longValue(), 
            "Nueva solicitud: " + oferta.getTitulo(), guardada.getIdTrabajador());

        return convertirADto(guardada);
    }

    // ==========================================
    // 2. CAMBIAR ESTADO (La Máquina de Estados)
    // ==========================================
    @Transactional
    public CitaServicioResponseDTO cambiarEstadoCita(Integer idCita, String nuevoCodigo, Integer idUser) {
        CitaServicio cita = citaRepository.findById(idCita)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada."));

        String actual = cita.getEstado().getCodigo();
        boolean esC = cita.getIdCliente().equals(idUser);
        boolean esT = cita.getIdTrabajador().equals(idUser);

        if (!esC && !esT) throw new RuntimeException("Sin permisos sobre esta cita.");

        String msg = "";
        Integer destino = null;

        switch (nuevoCodigo) {
            case "CITA_RECHAZADA":
                if (!esT) throw new RuntimeException("Solo el trabajador puede rechazar.");
                if (!actual.equals("CITA_PENDIENTE")) throw new RuntimeException("No se puede rechazar.");
                msg = "El trabajador ha rechazado la solicitud.";
                destino = cita.getIdCliente();
                break;

            case "CITA_HANDSHAKE":
                if (!esT) throw new RuntimeException("Solo el trabajador acepta el inicio.");
                if (!actual.equals("CITA_PENDIENTE")) throw new RuntimeException("Estado no válido.");
                msg = "¡Cita aceptada! Handshake realizado.";
                destino = cita.getIdCliente();
                break;

            case "CITA_COMENZANDO":
                if (!esT) throw new RuntimeException("Solo el trabajador marca llegada.");
                if (!actual.equals("CITA_HANDSHAKE")) throw new RuntimeException("Debe haber handshake.");
                msg = "El trabajador llegó al lugar. ¡Confirma el inicio!";
                destino = cita.getIdCliente();
                break;

            case "CITA_EN_PROCESO":
                if (!esC) throw new RuntimeException("Solo el cliente confirma el inicio.");
                if (!actual.equals("CITA_COMENZANDO")) throw new RuntimeException("El trabajador no ha llegado.");
                msg = "¡Servicio iniciado y en proceso!";
                destino = cita.getIdTrabajador();
                break;

            case "CITA_FINALIZANDO":
                if (!esT) throw new RuntimeException("Solo el trabajador indica término.");
                if (!actual.equals("CITA_EN_PROCESO")) throw new RuntimeException("No está en proceso.");
                msg = "El trabajador terminó. Confirma para finalizar.";
                destino = cita.getIdCliente();
                break;

            case "CITA_FINALIZADO":
                if (!esC) throw new RuntimeException("Solo el cliente finaliza el ciclo.");
                if (!actual.equals("CITA_FINALIZANDO")) throw new RuntimeException("Aún no termina el trabajo.");
                msg = "¡Servicio finalizado con éxito!";
                destino = cita.getIdTrabajador();
                break;

            case "CITA_CANCELADO":
                if (!esC) throw new RuntimeException("Solo el cliente puede cancelar.");
                if (actual.equals("CITA_FINALIZADO")) throw new RuntimeException("Ya está finalizado.");
                msg = "El cliente canceló la cita.";
                destino = cita.getIdTrabajador();
                break;

            default: throw new RuntimeException("Estado inválido.");
        }

        Estado nuevoEstado = estadoRepository.findByCodigo(nuevoCodigo)
                .orElseThrow(() -> new RuntimeException("Estado inexistente."));

        cita.setEstado(nuevoEstado);
        CitaServicio guardada = citaRepository.save(cita);

        // NOTIFICACIÓN WEB SOCKET: Se envía al otro usuario involucrado
        enviarNotificacion("CAMBIO_ESTADO_" + nuevoCodigo, guardada.getId().longValue(), msg, destino);

        return convertirADto(guardada);
    }

    private CitaServicioResponseDTO convertirADto(CitaServicio cita) {
        CitaServicioResponseDTO dto = new CitaServicioResponseDTO();
        dto.setId(cita.getId());
        dto.setComentario(cita.getComentario());
        dto.setFechaSolicitud(cita.getFechaSolicitud());
        dto.setIdOfertaServicio(cita.getOfertaServicio().getId());
        dto.setTituloOferta(cita.getOfertaServicio().getTitulo());
        dto.setIdCliente(cita.getIdCliente());
        dto.setIdTrabajador(cita.getIdTrabajador());
        dto.setEstado(cita.getEstado().getNombre());
        return dto;
    }

    private void enviarNotificacion(String tipo, Long id, String msg, Integer idDestino) {
        if (idDestino != null) {
            NotificacionWebSocketDTO noti = new NotificacionWebSocketDTO(tipo, id, msg, LocalDateTime.now());
            // Se envía al canal privado del usuario: /topic/notificaciones/{id}
            messagingTemplate.convertAndSend("/topic/notificaciones/" + idDestino, noti);
        }
    }
}