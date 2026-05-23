package com.contrabajo.servicios_api.service;

import com.contrabajo.servicios_api.dto.CitaServicioResponseDTO;
import com.contrabajo.servicios_api.dto.SolicitarCitaDTO;
import com.contrabajo.servicios_api.model.CategoriaServicio;
import com.contrabajo.servicios_api.model.CitaServicio;
import com.contrabajo.servicios_api.model.Estado;
import com.contrabajo.servicios_api.model.OfertaServicio;
import com.contrabajo.servicios_api.repository.CitaServicioRepository;
import com.contrabajo.servicios_api.repository.EstadoRepository;
import com.contrabajo.servicios_api.repository.OfertaServicioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CitaServicioServiceTest {

    @Mock
    private CitaServicioRepository citaRepository;

    @Mock
    private OfertaServicioRepository ofertaRepository;

    @Mock
    private EstadoRepository estadoRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private CitaServicioService citaService;

    private SolicitarCitaDTO solicitarDTO;
    private CitaServicio citaMock;
    private OfertaServicio ofertaMock;
    private CategoriaServicio categoriaMock;

    private Estado estadoPendiente;
    private Estado estadoHandshake;
    private Estado estadoComenzando;
    private Estado estadoEnProceso;
    private Estado estadoFinalizando;
    private Estado estadoFinalizado;
    private Estado estadoRechazado;
    private Estado estadoCancelado;

    private Integer idCliente   = 1;
    private Integer idTrabajador = 2;
    private Integer idCita       = 1;
    private Integer idOferta     = 1;

    @BeforeEach
    void setUp() {
        // Setup estados
        estadoPendiente  = estado((short) 1, "Pendiente",   "CITA_PENDIENTE");
        estadoHandshake  = estado((short) 2, "Handshake",   "CITA_HANDSHAKE");
        estadoComenzando = estado((short) 3, "Comenzando",  "CITA_COMENZANDO");
        estadoEnProceso  = estado((short) 4, "En proceso",  "CITA_EN_PROCESO");
        estadoFinalizando= estado((short) 5, "Finalizando", "CITA_FINALIZANDO");
        estadoFinalizado = estado((short) 6, "Finalizado",  "CITA_FINALIZADO");
        estadoRechazado  = estado((short) 7, "Rechazada",   "CITA_RECHAZADA");
        estadoCancelado  = estado((short) 8, "Cancelado",   "CITA_CANCELADO");

        // Setup categoría
        categoriaMock = new CategoriaServicio();
        categoriaMock.setId(1);
        categoriaMock.setNombre("Reparación");

        // Setup oferta
        ofertaMock = new OfertaServicio();
        ofertaMock.setId(idOferta);
        ofertaMock.setTitulo("Reparación de electrodomésticos");
        ofertaMock.setDisponible(true);
        ofertaMock.setBorrado(false);
        ofertaMock.setIdTrabajador(idTrabajador);
        ofertaMock.setCategoriaServicio(categoriaMock);

        // Setup cita en estado PENDIENTE
        citaMock = new CitaServicio();
        citaMock.setId(idCita);
        citaMock.setComentario("Necesito reparación urgente");
        citaMock.setFechaSolicitud(LocalDateTime.now());
        citaMock.setOfertaServicio(ofertaMock);
        citaMock.setCategoriaServicio(categoriaMock);
        citaMock.setIdTrabajador(idTrabajador);
        citaMock.setIdCliente(idCliente);
        citaMock.setEstado(estadoPendiente);

        // Setup DTO — la nueva versión lleva idCoordenadas dentro del DTO
        solicitarDTO = new SolicitarCitaDTO();
        solicitarDTO.setIdOfertaServicio(idOferta);
        solicitarDTO.setComentario("Necesito reparación urgente");
    }

    // ──────────────────────────────────────────────────────────────────────────
    // SOLICITAR SERVICIO
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    void testSolicitarServicio_Exitoso() {
        when(ofertaRepository.findById(idOferta)).thenReturn(Optional.of(ofertaMock));
        when(estadoRepository.findByCodigo("CITA_PENDIENTE")).thenReturn(Optional.of(estadoPendiente));
        when(citaRepository.save(any(CitaServicio.class))).thenReturn(citaMock);

        // Nueva firma: solicitarServicio(dto, idCliente) — sin idCoordenadas aparte
        CitaServicioResponseDTO resultado = citaService.solicitarServicio(solicitarDTO, idCliente);

        assertNotNull(resultado);
        verify(citaRepository, times(1)).save(any(CitaServicio.class));
        verify(messagingTemplate, times(1)).convertAndSend(anyString(), (Object) any());
    }

    @Test
    void testSolicitarServicio_OfertaNoEncontrada() {
        when(ofertaRepository.findById(idOferta)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> citaService.solicitarServicio(solicitarDTO, idCliente));
        assertTrue(ex.getMessage().contains("oferta") || ex.getMessage().contains("Oferta"));
        verify(citaRepository, never()).save(any());
    }

    @Test
    void testSolicitarServicio_OfertaNoDisponible() {
        ofertaMock.setDisponible(false);
        when(ofertaRepository.findById(idOferta)).thenReturn(Optional.of(ofertaMock));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> citaService.solicitarServicio(solicitarDTO, idCliente));
        assertTrue(ex.getMessage().contains("disponible") || ex.getMessage().contains("Disponible"));
        verify(citaRepository, never()).save(any());
    }

    @Test
    void testSolicitarServicio_OfertaBorrada() {
        ofertaMock.setBorrado(true);
        when(ofertaRepository.findById(idOferta)).thenReturn(Optional.of(ofertaMock));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> citaService.solicitarServicio(solicitarDTO, idCliente));
        assertTrue(ex.getMessage().contains("disponible") || ex.getMessage().contains("Disponible"));
        verify(citaRepository, never()).save(any());
    }

    @Test
    void testSolicitarServicio_TrabajadorSolicitandoSuPropio() {
        // El trabajador no puede solicitar su propio servicio
        when(ofertaRepository.findById(idOferta)).thenReturn(Optional.of(ofertaMock));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> citaService.solicitarServicio(solicitarDTO, idTrabajador)); // mismo ID que el dueño
        assertTrue(ex.getMessage().contains("mismo") || ex.getMessage().contains("propio"));
        verify(citaRepository, never()).save(any());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // ACEPTAR — PENDIENTE → HANDSHAKE (trabajador)
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    void testAceptarCita_Exitoso() {
        when(citaRepository.findById(idCita)).thenReturn(Optional.of(citaMock));
        when(estadoRepository.findByCodigo("CITA_HANDSHAKE")).thenReturn(Optional.of(estadoHandshake));
        when(citaRepository.save(any(CitaServicio.class))).thenReturn(citaMock);

        citaService.aceptarCita(idCita, idTrabajador);

        verify(citaRepository, times(1)).save(any(CitaServicio.class));
    }

    @Test
    void testAceptarCita_SinPermisos() {
        when(citaRepository.findById(idCita)).thenReturn(Optional.of(citaMock));

        // El cliente no puede aceptar
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> citaService.aceptarCita(idCita, idCliente));
        assertTrue(ex.getMessage().contains("trabajador") || ex.getMessage().contains("Trabajador"));
    }

    @Test
    void testAceptarCita_NoEncontrada() {
        when(citaRepository.findById(999)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> citaService.aceptarCita(999, idTrabajador));
        assertTrue(ex.getMessage().contains("Cita") || ex.getMessage().contains("cita"));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // RECHAZAR — PENDIENTE → RECHAZADA (trabajador)
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    void testRechazarCita_Exitoso() {
        when(citaRepository.findById(idCita)).thenReturn(Optional.of(citaMock));
        when(estadoRepository.findByCodigo("CITA_RECHAZADA")).thenReturn(Optional.of(estadoRechazado));
        when(citaRepository.save(any(CitaServicio.class))).thenReturn(citaMock);

        citaService.rechazarCita(idCita, idTrabajador);

        verify(citaRepository, times(1)).save(any(CitaServicio.class));
        verify(messagingTemplate, times(1)).convertAndSend(anyString(), (Object) any());
    }

    @Test
    void testRechazarCita_SinPermisos() {
        when(citaRepository.findById(idCita)).thenReturn(Optional.of(citaMock));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> citaService.rechazarCita(idCita, idCliente));
        assertTrue(ex.getMessage().contains("trabajador") || ex.getMessage().contains("Trabajador"));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // COMENZAR — HANDSHAKE → COMENZANDO (trabajador)
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    void testComenzarTrabajo_Exitoso() {
        citaMock.setEstado(estadoHandshake);
        when(citaRepository.findById(idCita)).thenReturn(Optional.of(citaMock));
        when(estadoRepository.findByCodigo("CITA_COMENZANDO")).thenReturn(Optional.of(estadoComenzando));
        when(citaRepository.save(any(CitaServicio.class))).thenReturn(citaMock);

        citaService.comenzarTrabajo(idCita, idTrabajador);

        verify(citaRepository, times(1)).save(any(CitaServicio.class));
    }

    @Test
    void testComenzarTrabajo_SinPermisos() {
        citaMock.setEstado(estadoHandshake);
        when(citaRepository.findById(idCita)).thenReturn(Optional.of(citaMock));

        // El cliente no puede iniciar el trabajo
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> citaService.comenzarTrabajo(idCita, idCliente));
        assertTrue(ex.getMessage().contains("trabajador") || ex.getMessage().contains("Trabajador"));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // CONFIRMAR INICIO — COMENZANDO → EN_PROCESO (cliente)
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    void testConfirmarInicio_Exitoso() {
        citaMock.setEstado(estadoComenzando);
        when(citaRepository.findById(idCita)).thenReturn(Optional.of(citaMock));
        when(estadoRepository.findByCodigo("CITA_EN_PROCESO")).thenReturn(Optional.of(estadoEnProceso));
        when(citaRepository.save(any(CitaServicio.class))).thenReturn(citaMock);

        citaService.confirmarInicio(idCita, idCliente);

        verify(citaRepository, times(1)).save(any(CitaServicio.class));
    }

    @Test
    void testConfirmarInicio_TransicionInvalida() {
        // Estado incorrecto: no está en COMENZANDO
        citaMock.setEstado(estadoPendiente);
        when(citaRepository.findById(idCita)).thenReturn(Optional.of(citaMock));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> citaService.confirmarInicio(idCita, idCliente));
        assertNotNull(ex.getMessage()); // Debe lanzar algún error de validación de estado
    }

    // ──────────────────────────────────────────────────────────────────────────
    // FINALIZAR TRABAJO — EN_PROCESO → FINALIZANDO (trabajador)
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    void testFinalizarTrabajo_Exitoso() {
        citaMock.setEstado(estadoEnProceso);
        when(citaRepository.findById(idCita)).thenReturn(Optional.of(citaMock));
        when(estadoRepository.findByCodigo("CITA_FINALIZANDO")).thenReturn(Optional.of(estadoFinalizando));
        when(citaRepository.save(any(CitaServicio.class))).thenReturn(citaMock);

        citaService.finalizarTrabajo(idCita, idTrabajador);

        verify(citaRepository, times(1)).save(any(CitaServicio.class));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // CONFIRMAR FINALIZACION — FINALIZANDO → FINALIZADO (cliente)
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    void testConfirmarFinalizacion_Exitoso() {
        citaMock.setEstado(estadoFinalizando);
        when(citaRepository.findById(idCita)).thenReturn(Optional.of(citaMock));
        when(estadoRepository.findByCodigo("CITA_FINALIZADO")).thenReturn(Optional.of(estadoFinalizado));
        when(citaRepository.save(any(CitaServicio.class))).thenReturn(citaMock);

        citaService.confirmarFinalizacion(idCita, idCliente);

        verify(citaRepository, times(1)).save(any(CitaServicio.class));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // CANCELAR — cualquier estado activo → CANCELADO
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    void testCancelarCita_Exitoso() {
        citaMock.setEstado(estadoHandshake);
        when(citaRepository.findById(idCita)).thenReturn(Optional.of(citaMock));
        when(estadoRepository.findByCodigo("CITA_CANCELADO")).thenReturn(Optional.of(estadoCancelado));
        when(citaRepository.save(any(CitaServicio.class))).thenReturn(citaMock);

        citaService.cancelarCita(idCita, idCliente);

        verify(citaRepository, times(1)).save(any(CitaServicio.class));
    }

    @Test
    void testCancelarCita_YaFinalizada() {
        citaMock.setEstado(estadoFinalizado);
        when(citaRepository.findById(idCita)).thenReturn(Optional.of(citaMock));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> citaService.cancelarCita(idCita, idCliente));
        assertTrue(ex.getMessage().contains("finalizada") || ex.getMessage().contains("cancelada"));
    }

    @Test
    void testCancelarCita_SinPermisos() {
        // Usuario que no es ni cliente ni trabajador
        when(citaRepository.findById(idCita)).thenReturn(Optional.of(citaMock));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> citaService.cancelarCita(idCita, 999));
        assertTrue(ex.getMessage().contains("denegado") || ex.getMessage().contains("permisos"));
    }

    @Test
    void testCancelarCita_NoEncontrada() {
        when(citaRepository.findById(999)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> citaService.cancelarCita(999, idCliente));
        assertTrue(ex.getMessage().contains("Cita") || ex.getMessage().contains("cita"));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // NOTIFICACIÓN enviada correctamente al rechazar
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    void testRechazarCita_NotificacionEnviadaAlCliente() {
        when(citaRepository.findById(idCita)).thenReturn(Optional.of(citaMock));
        when(estadoRepository.findByCodigo("CITA_RECHAZADA")).thenReturn(Optional.of(estadoRechazado));
        when(citaRepository.save(any(CitaServicio.class))).thenReturn(citaMock);

        citaService.rechazarCita(idCita, idTrabajador);

        // El servicio notifica en /topic/notificaciones/{idCliente}
        verify(messagingTemplate, times(1)).convertAndSend(
                contains("/topic/notificaciones/" + idCliente),
                (Object) any()
        );
    }

    // ──────────────────────────────────────────────────────────────────────────
    // HELPER
    // ──────────────────────────────────────────────────────────────────────────

    private Estado estado(short id, String nombre, String codigo) {
        Estado e = new Estado();
        e.setId(id);
        e.setNombre(nombre);
        e.setCodigo(codigo);
        return e;
    }
}
