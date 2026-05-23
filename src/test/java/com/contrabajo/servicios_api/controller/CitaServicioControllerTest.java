package com.contrabajo.servicios_api.controller;

import com.contrabajo.servicios_api.dto.CitaServicioResponseDTO;
import com.contrabajo.servicios_api.dto.SolicitarCitaDTO;
import com.contrabajo.servicios_api.service.CitaServicioService;
import com.contrabajo.servicios_api.utils.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CitaServicioControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CitaServicioService citaService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private CitaServicioController citaController;

    private ObjectMapper objectMapper;
    private SolicitarCitaDTO solicitarDTO;
    private CitaServicioResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(citaController).build();
        objectMapper = new ObjectMapper();

        // Setup DTO solicitar
        solicitarDTO = new SolicitarCitaDTO();
        solicitarDTO.setIdOfertaServicio(1);
        solicitarDTO.setComentario("Necesito reparación urgente");

        // Setup response DTO
        responseDTO = new CitaServicioResponseDTO();
        responseDTO.setId(1);
        responseDTO.setIdOfertaServicio(1);
        responseDTO.setIdCliente(1);
        responseDTO.setIdTrabajador(2);
        responseDTO.setTituloOferta("Reparación de electrodomésticos");
    }

    // ==========================================
    // Test: Solicitar servicio exitosamente
    // ==========================================
    @Test
    void testSolicitar_Exitoso() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer token123");
        when(jwtUtil.extractId("token123")).thenReturn(1);
        // Nueva firma: solicitarServicio(dto, idCliente) — sin idCoordenadas aparte
        when(citaService.solicitarServicio(any(), anyInt())).thenReturn(responseDTO);

        mockMvc.perform(post("/api/citas/solicitar")
                .header("Authorization", "Bearer token123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(solicitarDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.idOfertaServicio").value(1));

        verify(citaService, times(1)).solicitarServicio(any(), anyInt());
    }

    // ==========================================
    // Test: Solicitar servicio con oferta no disponible
    // ==========================================
    @Test
    void testSolicitar_OfertaNoDisponible() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer token123");
        when(jwtUtil.extractId("token123")).thenReturn(1);
        doThrow(new RuntimeException("Esta oferta de servicio ya no esta disponible."))
                .when(citaService).solicitarServicio(any(), anyInt());

        mockMvc.perform(post("/api/citas/solicitar")
                .header("Authorization", "Bearer token123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(solicitarDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Esta oferta de servicio ya no esta disponible."));
    }

    // ==========================================
    // Test: Solicitar sin token
    // ==========================================
    @Test
    void testSolicitar_SinToken() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        mockMvc.perform(post("/api/citas/solicitar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(solicitarDTO)))
                .andExpect(status().isBadRequest());
    }

    // ==========================================
    // Test: Aceptar cita (trabajador) → /aceptar
    // ==========================================
    @Test
    void testAceptar_Exitoso() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer token123");
        when(jwtUtil.extractId("token123")).thenReturn(2);
        when(citaService.aceptarCita(anyInt(), anyInt())).thenReturn(responseDTO);

        mockMvc.perform(patch("/api/citas/{id}/aceptar", 1)
                .header("Authorization", "Bearer token123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(citaService, times(1)).aceptarCita(1, 2);
    }

    // ==========================================
    // Test: Aceptar sin token
    // ==========================================
    @Test
    void testAceptar_SinToken() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        mockMvc.perform(patch("/api/citas/{id}/aceptar", 1))
                .andExpect(status().isBadRequest());
    }

    // ==========================================
    // Test: Rechazar cita (trabajador) → /rechazar
    // ==========================================
    @Test
    void testRechazar_Exitoso() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer token123");
        when(jwtUtil.extractId("token123")).thenReturn(2);
        when(citaService.rechazarCita(anyInt(), anyInt())).thenReturn(responseDTO);

        mockMvc.perform(patch("/api/citas/{id}/rechazar", 1)
                .header("Authorization", "Bearer token123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(citaService, times(1)).rechazarCita(1, 2);
    }

    // ==========================================
    // Test: Rechazar — error de permisos
    // ==========================================
    @Test
    void testRechazar_Error() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer token123");
        when(jwtUtil.extractId("token123")).thenReturn(1);
        doThrow(new RuntimeException("Solo el trabajador puede realizar esta accion."))
                .when(citaService).rechazarCita(anyInt(), anyInt());

        mockMvc.perform(patch("/api/citas/{id}/rechazar", 1)
                .header("Authorization", "Bearer token123"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Solo el trabajador puede realizar esta accion."));
    }

    // ==========================================
    // Test: Comenzar cita (trabajador) → /comenzar
    // ==========================================
    @Test
    void testComenzar_Exitoso() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer token123");
        when(jwtUtil.extractId("token123")).thenReturn(2);
        when(citaService.comenzarTrabajo(anyInt(), anyInt())).thenReturn(responseDTO);

        mockMvc.perform(patch("/api/citas/{id}/comenzar", 1)
                .header("Authorization", "Bearer token123"))
                .andExpect(status().isOk());

        verify(citaService, times(1)).comenzarTrabajo(1, 2);
    }

    // ==========================================
    // Test: Finalizar trabajo (trabajador) → /finalizar
    // ==========================================
    @Test
    void testFinalizar_Exitoso() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer token123");
        when(jwtUtil.extractId("token123")).thenReturn(2);
        when(citaService.finalizarTrabajo(anyInt(), anyInt())).thenReturn(responseDTO);

        mockMvc.perform(patch("/api/citas/{id}/finalizar", 1)
                .header("Authorization", "Bearer token123"))
                .andExpect(status().isOk());

        verify(citaService, times(1)).finalizarTrabajo(1, 2);
    }

    // ==========================================
    // Test: Confirmar inicio (cliente) → /confirmar-inicio
    // ==========================================
    @Test
    void testConfirmarInicio_Exitoso() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer token123");
        when(jwtUtil.extractId("token123")).thenReturn(1);
        when(citaService.confirmarInicio(anyInt(), anyInt())).thenReturn(responseDTO);

        mockMvc.perform(patch("/api/citas/{id}/confirmar-inicio", 1)
                .header("Authorization", "Bearer token123"))
                .andExpect(status().isOk());

        verify(citaService, times(1)).confirmarInicio(1, 1);
    }

    // ==========================================
    // Test: Confirmar finalización (cliente) → /confirmar-finalizacion
    // ==========================================
    @Test
    void testConfirmarFinalizacion_Exitoso() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer token123");
        when(jwtUtil.extractId("token123")).thenReturn(1);
        when(citaService.confirmarFinalizacion(anyInt(), anyInt())).thenReturn(responseDTO);

        mockMvc.perform(patch("/api/citas/{id}/confirmar-finalizacion", 1)
                .header("Authorization", "Bearer token123"))
                .andExpect(status().isOk());

        verify(citaService, times(1)).confirmarFinalizacion(1, 1);
    }

    // ==========================================
    // Test: Cancelar cita (cualquiera) → /cancelar
    // ==========================================
    @Test
    void testCancelar_Exitoso() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer token123");
        when(jwtUtil.extractId("token123")).thenReturn(1);
        when(citaService.cancelarCita(anyInt(), anyInt())).thenReturn(responseDTO);

        mockMvc.perform(patch("/api/citas/{id}/cancelar", 1)
                .header("Authorization", "Bearer token123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(citaService, times(1)).cancelarCita(1, 1);
    }

    // ==========================================
    // Test: Cancelar cita ya finalizada
    // ==========================================
    @Test
    void testCancelar_YaFinalizada() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer token123");
        when(jwtUtil.extractId("token123")).thenReturn(1);
        doThrow(new RuntimeException("No se puede cancelar una cita ya finalizada o cancelada."))
                .when(citaService).cancelarCita(anyInt(), anyInt());

        mockMvc.perform(patch("/api/citas/{id}/cancelar", 1)
                .header("Authorization", "Bearer token123"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("No se puede cancelar una cita ya finalizada o cancelada."));
    }

    // ==========================================
    // Test: Cambio de estado — error de permisos genérico
    // ==========================================
    @Test
    void testCambiarEstado_Error() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer token123");
        when(jwtUtil.extractId("token123")).thenReturn(2);
        doThrow(new RuntimeException("Solo el trabajador puede realizar esta accion."))
                .when(citaService).aceptarCita(anyInt(), anyInt());

        mockMvc.perform(patch("/api/citas/{id}/aceptar", 1)
                .header("Authorization", "Bearer token123"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Solo el trabajador puede realizar esta accion."));
    }
}
