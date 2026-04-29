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
    
    // ¡LA MAGIA DE WEBSOCKETS INYECTADA AQUÍ!
    private final SimpMessagingTemplate messagingTemplate;

    // ==========================================
    // 1. SOLICITAR CITA 
    // ==========================================
    @Transactional
    public CitaServicioResponseDTO solicitarServicio(SolicitarCitaDTO dto, Integer idCliente, Integer idCoordenadas) {
        OfertaServicio oferta = ofertaRepository.findById(dto.getIdOfertaServicio())
                .orElseThrow(() -> new RuntimeException("La oferta de servicio no existe."));

        if (!oferta.getDisponible() || oferta.getBorrado()) {
            throw new RuntimeException("Esta oferta de servicio ya no está disponible.");
        }
        
        if (oferta.getIdTrabajador().equals(idCliente)) {
            throw new RuntimeException("No puedes solicitar un servicio publicado por ti mismo.");
        }

        Estado estadoPendiente = estadoRepository.findByCodigo("PEND")
                .orElseThrow(() -> new RuntimeException("Error interno: Estado Pendiente no configurado."));

        CitaServicio nuevaCita = new CitaServicio();
        nuevaCita.setComentario(dto.getComentario());
        
        // ==========================================
        // CAMBIO CRÍTICO: Usamos el ID que viene del Token, no del DTO
        // ==========================================
        nuevaCita.setIdCoordenadas(idCoordenadas); 
        
        nuevaCita.setFechaSolicitud(LocalDateTime.now());
        nuevaCita.setOfertaServicio(oferta);
        nuevaCita.setCategoriaServicio(oferta.getCategoriaServicio());
        nuevaCita.setIdTrabajador(oferta.getIdTrabajador());
        nuevaCita.setIdCliente(idCliente);
        nuevaCita.setEstado(estadoPendiente);

        CitaServicio guardada = citaRepository.save(nuevaCita);

        // Notificación al trabajador
        enviarNotificacion(
            "NUEVO_SERVICIO", 
            guardada.getId().longValue(), 
            "¡Tienes una nueva solicitud de servicio para: " + oferta.getTitulo() + "!",
            guardada.getIdTrabajador()
        );

        return convertirADto(guardada);
    }

    // ==========================================
    // 2. CAMBIAR ESTADO (Aceptar, Cancelar, Finalizar)
    // ==========================================
    @Transactional
    public CitaServicioResponseDTO cambiarEstadoCita(Integer idCita, String nuevoCodigoEstado, Integer idUsuarioAutenticado) {
        CitaServicio cita = citaRepository.findById(idCita)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada."));

        boolean esCliente = cita.getIdCliente().equals(idUsuarioAutenticado);
        boolean esTrabajador = cita.getIdTrabajador().equals(idUsuarioAutenticado);

        if (!esCliente && !esTrabajador) {
            throw new RuntimeException("Acceso denegado: No tienes permisos sobre esta cita.");
        }

        String mensajeNotificacion = "";
        Integer idDestinatarioNotificacion = null;

        if (nuevoCodigoEstado.equals("ACEP")) {
            if (!esTrabajador) throw new RuntimeException("Solo el trabajador puede aceptar la cita.");
            if (!cita.getEstado().getCodigo().equals("PEND")) throw new RuntimeException("Solo se pueden aceptar citas pendientes.");
            
            // Si el trabajador acepta, notificamos al CLIENTE
            mensajeNotificacion = "¡Tu cita ha sido ACEPTADA por el trabajador!";
            idDestinatarioNotificacion = cita.getIdCliente();
        } 
        else if (nuevoCodigoEstado.equals("CANC")) {
            // Si yo cancelo, notifico a la otra parte
            mensajeNotificacion = "La cita ha sido CANCELADA.";
            idDestinatarioNotificacion = esCliente ? cita.getIdTrabajador() : cita.getIdCliente();
        }
        else if (nuevoCodigoEstado.equals("FINA")) {
            if (!cita.getEstado().getCodigo().equals("ACEP")) throw new RuntimeException("El trabajo debe estar aceptado antes de finalizarse.");
            
            // Si se finaliza, notificamos al CLIENTE
            mensajeNotificacion = "El trabajo ha sido marcado como FINALIZADO.";
            idDestinatarioNotificacion = cita.getIdCliente();
        }

        Estado nuevoEstado = estadoRepository.findByCodigo(nuevoCodigoEstado)
                .orElseThrow(() -> new RuntimeException("Estado " + nuevoCodigoEstado + " no válido."));

        cita.setEstado(nuevoEstado);
        CitaServicio actualizada = citaRepository.save(cita);

        // ==========================================
        // ENVIAR NOTIFICACIÓN DE CAMBIO DE ESTADO AL FRONTEND
        // ==========================================
        enviarNotificacion(
            "CAMBIO_ESTADO_" + nuevoCodigoEstado, // Ej: "CAMBIO_ESTADO_ACEP"
            actualizada.getId().longValue(), 
            mensajeNotificacion,
            idDestinatarioNotificacion
        );

        return convertirADto(actualizada);
    }

    // ==========================================
    // UTILS: Traductor a DTO
    // ==========================================
    private CitaServicioResponseDTO convertirADto(CitaServicio cita) {
        // ... (el mismo código de conversión a DTO que teníamos antes)
        CitaServicioResponseDTO dto = new CitaServicioResponseDTO();
        dto.setId(cita.getId());
        dto.setComentario(cita.getComentario());
        dto.setFechaSolicitud(cita.getFechaSolicitud());
        dto.setIdOfertaServicio(cita.getOfertaServicio().getId());
        dto.setTituloOferta(cita.getOfertaServicio().getTitulo());
        dto.setIdCliente(cita.getIdCliente());
        dto.setIdTrabajador(cita.getIdTrabajador());
        if (cita.getEstado() != null) dto.setEstado(cita.getEstado().getNombre());
        return dto;
    }

    // ==========================================
    // UTILS: Disparador de WebSockets
    // ==========================================
    private void enviarNotificacion(String tipo, Long idServicio, String mensaje, Integer idUsuarioDestino) {
        NotificacionWebSocketDTO notificacion = new NotificacionWebSocketDTO(
                tipo,
                idServicio,
                mensaje,
                LocalDateTime.now()
        );

        // OPCIÓN A (Global): Como lo tenías en tu prueba, se envía a TODOS. 
        // messagingTemplate.convertAndSend("/topic/servicios", notificacion);

        // OPCIÓN B (Específico y Recomendado): Se envía a un canal único para el usuario afectado.
        // En tu frontend de Kotlin, el usuario se suscribiría a: "/topic/notificaciones/" + su_id
        messagingTemplate.convertAndSend("/topic/notificaciones/" + idUsuarioDestino, notificacion);
    }
}