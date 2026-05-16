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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CitaServicioService {

    // Codigos de estado — fuente de verdad: DataSeeder.java (coincide con EstadoCita del front)
    private static final String CITA_PENDIENTE   = "CITA_PENDIENTE";   // 401
    private static final String CITA_HANDSHAKE   = "CITA_HANDSHAKE";   // 402
    private static final String CITA_COMENZANDO  = "CITA_COMENZANDO";  // 403
    private static final String CITA_EN_PROCESO  = "CITA_EN_PROCESO";  // 404
    private static final String CITA_FINALIZANDO = "CITA_FINALIZANDO"; // 405
    private static final String CITA_FINALIZADO  = "CITA_FINALIZADO";  // 406
    private static final String CITA_CANCELADO   = "CITA_CANCELADO";   // 407
    private static final String CITA_RECHAZADA   = "CITA_RECHAZADA";   // 409

    private final CitaServicioRepository citaRepository;
    private final OfertaServicioRepository ofertaRepository;
    private final EstadoRepository estadoRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // ──────────────────────────────────────────────────────────────────────────
    // 1. SOLICITAR — crea la cita en estado PENDIENTE (cliente)
    // ──────────────────────────────────────────────────────────────────────────
    @Transactional
    public CitaServicioResponseDTO solicitarServicio(SolicitarCitaDTO dto, Integer idCliente) {
        OfertaServicio oferta = ofertaRepository.findById(dto.getIdOfertaServicio())
                .orElseThrow(() -> new RuntimeException("La oferta de servicio no existe."));

        if (!oferta.getDisponible() || oferta.getBorrado()) {
            throw new RuntimeException("Esta oferta de servicio ya no esta disponible.");
        }
        if (oferta.getIdTrabajador().equals(idCliente)) {
            throw new RuntimeException("No puedes solicitar un servicio publicado por ti mismo.");
        }

        Estado estadoPendiente = buscarEstado(CITA_PENDIENTE);

        CitaServicio nuevaCita = new CitaServicio();
        nuevaCita.setComentario(dto.getComentario());
        nuevaCita.setIdCoordenadas(dto.getIdCoordenadas());
        nuevaCita.setFechaSolicitud(LocalDateTime.now());
        nuevaCita.setOfertaServicio(oferta);
        nuevaCita.setCategoriaServicio(oferta.getCategoriaServicio());
        nuevaCita.setIdTrabajador(oferta.getIdTrabajador());
        nuevaCita.setIdCliente(idCliente);
        nuevaCita.setEstado(estadoPendiente);

        CitaServicio guardada = citaRepository.save(nuevaCita);

        notificar("NUEVO_SERVICIO", guardada.getId().longValue(),
                "Tienes una nueva solicitud para: " + oferta.getTitulo(),
                guardada.getIdTrabajador());

        return toDto(guardada);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 2. ACEPTAR — PENDIENTE → HANDSHAKE (trabajador)
    // ──────────────────────────────────────────────────────────────────────────
    @Transactional
    public CitaServicioResponseDTO aceptarCita(Integer idCita, Integer idUsuario) {
        CitaServicio cita = obtenerVerificada(idCita, idUsuario);
        validarTrabajador(cita, idUsuario);
        validarEstado(cita, CITA_PENDIENTE, "Solo se pueden aceptar citas pendientes.");
        return transicionar(cita, CITA_HANDSHAKE,
                "Tu cita fue ACEPTADA. El trabajador esta listo para coordinar.", cita.getIdCliente());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 3. RECHAZAR — PENDIENTE → RECHAZADA (trabajador)
    // ──────────────────────────────────────────────────────────────────────────
    @Transactional
    public CitaServicioResponseDTO rechazarCita(Integer idCita, Integer idUsuario) {
        CitaServicio cita = obtenerVerificada(idCita, idUsuario);
        validarTrabajador(cita, idUsuario);
        validarEstado(cita, CITA_PENDIENTE, "Solo se pueden rechazar citas pendientes.");
        return transicionar(cita, CITA_RECHAZADA,
                "La cita fue RECHAZADA. Puedes reenviar una nueva propuesta.", cita.getIdCliente());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 4. REENVIAR PROPUESTA — RECHAZADA → PENDIENTE (cliente)
    // ──────────────────────────────────────────────────────────────────────────
    @Transactional
    public CitaServicioResponseDTO reenviarPropuesta(Integer idCita, Integer idUsuario) {
        CitaServicio cita = obtenerVerificada(idCita, idUsuario);
        validarCliente(cita, idUsuario);
        validarEstado(cita, CITA_RECHAZADA, "Solo se puede reenviar propuesta en citas rechazadas.");
        return transicionar(cita, CITA_PENDIENTE,
                "El cliente envio una nueva propuesta de cita.", cita.getIdTrabajador());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 5. COMENZAR — HANDSHAKE → COMENZANDO (trabajador solicita inicio)
    // ──────────────────────────────────────────────────────────────────────────
    @Transactional
    public CitaServicioResponseDTO comenzarTrabajo(Integer idCita, Integer idUsuario) {
        CitaServicio cita = obtenerVerificada(idCita, idUsuario);
        validarTrabajador(cita, idUsuario);
        validarEstado(cita, CITA_HANDSHAKE, "Solo se puede iniciar trabajo en citas aceptadas (handshake).");
        return transicionar(cita, CITA_COMENZANDO,
                "El trabajador quiere comenzar. Por favor confirma el inicio.", cita.getIdCliente());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 6. CONFIRMAR INICIO — COMENZANDO → EN_PROCESO (cliente confirma)
    // ──────────────────────────────────────────────────────────────────────────
    @Transactional
    public CitaServicioResponseDTO confirmarInicio(Integer idCita, Integer idUsuario) {
        CitaServicio cita = obtenerVerificada(idCita, idUsuario);
        validarCliente(cita, idUsuario);
        validarEstado(cita, CITA_COMENZANDO, "Solo se puede confirmar inicio en citas en estado Comenzando.");
        cita.setFechaInicioTrabajo(LocalDateTime.now());
        return transicionar(cita, CITA_EN_PROCESO,
                "El cliente confirmo el inicio del trabajo. El servicio esta EN PROCESO.", cita.getIdTrabajador());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 7. FINALIZAR — EN_PROCESO → FINALIZANDO (trabajador solicita cierre)
    // ──────────────────────────────────────────────────────────────────────────
    @Transactional
    public CitaServicioResponseDTO finalizarTrabajo(Integer idCita, Integer idUsuario) {
        CitaServicio cita = obtenerVerificada(idCita, idUsuario);
        validarTrabajador(cita, idUsuario);
        validarEstado(cita, CITA_EN_PROCESO, "Solo se puede solicitar finalizacion en citas en proceso.");
        return transicionar(cita, CITA_FINALIZANDO,
                "El trabajador solicita finalizar el trabajo. Por favor confirma.", cita.getIdCliente());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 8. CONFIRMAR FINALIZACION — FINALIZANDO → FINALIZADO (cliente confirma)
    // ──────────────────────────────────────────────────────────────────────────
    @Transactional
    public CitaServicioResponseDTO confirmarFinalizacion(Integer idCita, Integer idUsuario) {
        CitaServicio cita = obtenerVerificada(idCita, idUsuario);
        validarCliente(cita, idUsuario);
        validarEstado(cita, CITA_FINALIZANDO, "Solo se puede confirmar finalizacion en citas en estado Finalizando.");
        cita.setFechaFinTrabajo(LocalDateTime.now());
        return transicionar(cita, CITA_FINALIZADO,
                "El cliente confirmo la finalizacion. El trabajo queda COMPLETADO.", cita.getIdTrabajador());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 9. CANCELAR — cualquier estado activo → CANCELADO (cualquiera)
    // ──────────────────────────────────────────────────────────────────────────
    @Transactional
    public CitaServicioResponseDTO cancelarCita(Integer idCita, Integer idUsuario) {
        CitaServicio cita = obtenerVerificada(idCita, idUsuario);
        String estadoActual = cita.getEstado().getCodigo();
        if (CITA_FINALIZADO.equals(estadoActual) || CITA_CANCELADO.equals(estadoActual)) {
            throw new RuntimeException("No se puede cancelar una cita ya finalizada o cancelada.");
        }
        boolean esCliente = cita.getIdCliente().equals(idUsuario);
        Integer destinatario = esCliente ? cita.getIdTrabajador() : cita.getIdCliente();
        return transicionar(cita, CITA_CANCELADO, "La cita fue CANCELADA.", destinatario);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 10. LISTAR CITAS DEL USUARIO AUTENTICADO
    // ──────────────────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<CitaServicioResponseDTO> listarMisCitas(Integer idUsuario) {
        List<CitaServicio> comoCliente    = citaRepository.findByIdClienteOrderByFechaSolicitudDesc(idUsuario);
        List<CitaServicio> comoTrabajador = citaRepository.findByIdTrabajadorOrderByFechaSolicitudDesc(idUsuario);

        List<CitaServicio> todas = new ArrayList<>(comoCliente);
        for (CitaServicio c : comoTrabajador) {
            if (todas.stream().noneMatch(e -> e.getId().equals(c.getId()))) {
                todas.add(c);
            }
        }
        todas.sort((a, b) -> {
            if (a.getFechaSolicitud() == null) return 1;
            if (b.getFechaSolicitud() == null) return -1;
            return b.getFechaSolicitud().compareTo(a.getFechaSolicitud());
        });
        return todas.stream().map(this::toDto).collect(Collectors.toList());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 11. DETALLE DE UNA CITA
    // ──────────────────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public CitaServicioResponseDTO obtenerCita(Integer idCita, Integer idUsuario) {
        return toDto(obtenerVerificada(idCita, idUsuario));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // UTILIDADES INTERNAS
    // ──────────────────────────────────────────────────────────────────────────

    private CitaServicio obtenerVerificada(Integer idCita, Integer idUsuario) {
        CitaServicio cita = citaRepository.findById(idCita)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada."));
        if (!cita.getIdCliente().equals(idUsuario) && !cita.getIdTrabajador().equals(idUsuario)) {
            throw new RuntimeException("Acceso denegado: no tienes permisos sobre esta cita.");
        }
        return cita;
    }

    private void validarTrabajador(CitaServicio cita, Integer idUsuario) {
        if (!cita.getIdTrabajador().equals(idUsuario)) {
            throw new RuntimeException("Solo el trabajador puede realizar esta accion.");
        }
    }

    private void validarCliente(CitaServicio cita, Integer idUsuario) {
        if (!cita.getIdCliente().equals(idUsuario)) {
            throw new RuntimeException("Solo el cliente puede realizar esta accion.");
        }
    }

    private void validarEstado(CitaServicio cita, String codigoEsperado, String mensaje) {
        if (!codigoEsperado.equals(cita.getEstado().getCodigo())) {
            throw new RuntimeException(mensaje + " Estado actual: " + cita.getEstado().getNombre());
        }
    }

    private CitaServicioResponseDTO transicionar(CitaServicio cita, String nuevoCodigo,
                                                  String mensajeNotif, Integer destinatario) {
        Estado nuevoEstado = buscarEstado(nuevoCodigo);
        cita.setEstado(nuevoEstado);
        CitaServicio actualizada = citaRepository.save(cita);
        notificar("CAMBIO_ESTADO_" + nuevoCodigo, actualizada.getId().longValue(), mensajeNotif, destinatario);
        return toDto(actualizada);
    }

    private Estado buscarEstado(String codigo) {
        return estadoRepository.findByCodigo(codigo)
                .orElseThrow(() -> new RuntimeException("Estado " + codigo + " no configurado en BD."));
    }

    private void notificar(String tipo, Long idServicio, String mensaje, Integer idDestino) {
        if (idDestino == null) return;
        NotificacionWebSocketDTO notif = new NotificacionWebSocketDTO(tipo, idServicio, mensaje, LocalDateTime.now());
        messagingTemplate.convertAndSend("/topic/notificaciones/" + idDestino, notif);
    }

    private CitaServicioResponseDTO toDto(CitaServicio cita) {
        CitaServicioResponseDTO dto = new CitaServicioResponseDTO();
        dto.setId(cita.getId());
        dto.setComentario(cita.getComentario());
        dto.setFechaSolicitud(cita.getFechaSolicitud());
        dto.setFechaInicioTrabajo(cita.getFechaInicioTrabajo());
        dto.setFechaFinTrabajo(cita.getFechaFinTrabajo());
        dto.setIdOfertaServicio(cita.getOfertaServicio().getId());
        dto.setTituloOferta(cita.getOfertaServicio().getTitulo());
        dto.setIdCliente(cita.getIdCliente());
        dto.setIdTrabajador(cita.getIdTrabajador());
        if (cita.getEstado() != null) {
            dto.setIdEstado(cita.getEstado().getId() != null ? (int) cita.getEstado().getId() : null);
            dto.setCodigoEstado(cita.getEstado().getCodigo());
            dto.setEstado(cita.getEstado().getNombre());
        }
        return dto;
    }
}
