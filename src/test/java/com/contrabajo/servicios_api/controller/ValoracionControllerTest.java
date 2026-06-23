package com.contrabajo.servicios_api.controller;

import com.contrabajo.servicios_api.dto.ValoracionRequestDTO;
import com.contrabajo.servicios_api.dto.ValoracionResponseDTO;
import com.contrabajo.servicios_api.service.ValoracionService;
import com.contrabajo.servicios_api.utils.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ValoracionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ValoracionService valoracionService;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private ValoracionController valoracionController;

    private ObjectMapper objectMapper;
    private ValoracionRequestDTO valoracionDTO;
    private ValoracionResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(valoracionController).build();
        objectMapper = new ObjectMapper();

        // Setup DTO
        valoracionDTO = new ValoracionRequestDTO();
        valoracionDTO.setIdCita(1);
        valoracionDTO.setVoto((short) 5);
        valoracionDTO.setComentario("Excelente servicio");

        responseDTO = new ValoracionResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setIdCita(1);
        responseDTO.setIdCliente(1);
        responseDTO.setIdTrabajador(2);
        responseDTO.setVoto((short) 5);
        responseDTO.setComentario("Excelente servicio");
    }

    // ==========================================
    // Test: Crear valoración exitosamente
    // ==========================================
    @Test
    void testCrearValoracion_Exitoso() throws Exception {
        // Arrange
        when(jwtUtil.extractId("token123")).thenReturn(1);
        doNothing().when(valoracionService).crearValoracion(any(), anyInt());

        // Act & Assert
        mockMvc.perform(post("/api/valoraciones")
                .header("Authorization", "Bearer token123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(valoracionDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("La valoración ha sido registrada con éxito."));

        verify(valoracionService, times(1)).crearValoracion(any(), anyInt());
    }

    // ==========================================
    // Test: Crear valoración con voto inválido
    // ==========================================
    @Test
    void testCrearValoracion_VotoInvalido() throws Exception {
        // Arrange
        valoracionDTO.setVoto((short) 6); // Mayor a 5
        when(jwtUtil.extractId("token123")).thenReturn(1);
        doThrow(new RuntimeException("La valoración debe estar entre 1 y 5 estrellas."))
                .when(valoracionService).crearValoracion(any(), anyInt());

        // Act & Assert
        mockMvc.perform(post("/api/valoraciones")
                .header("Authorization", "Bearer token123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(valoracionDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("La valoración debe estar entre 1 y 5 estrellas."));

        verify(valoracionService, times(1)).crearValoracion(any(), anyInt());
    }

    // ==========================================
    // Test: Crear valoración con cita no encontrada
    // ==========================================
    @Test
    void testCrearValoracion_CitaNoEncontrada() throws Exception {
        // Arrange
        when(jwtUtil.extractId("token123")).thenReturn(1);
        doThrow(new RuntimeException("Cita no encontrada."))
                .when(valoracionService).crearValoracion(any(), anyInt());

        // Act & Assert
        mockMvc.perform(post("/api/valoraciones")
                .header("Authorization", "Bearer token123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(valoracionDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Cita no encontrada."));
    }

    // ==========================================
    // Test: Crear valoración sin permisos
    // ==========================================
    @Test
    void testCrearValoracion_SinPermisos() throws Exception {
        // Arrange
        when(jwtUtil.extractId("token123")).thenReturn(1);
        doThrow(new RuntimeException("Acceso denegado: No tienes permisos para valorar esta cita."))
                .when(valoracionService).crearValoracion(any(), anyInt());

        // Act & Assert
        mockMvc.perform(post("/api/valoraciones")
                .header("Authorization", "Bearer token123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(valoracionDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("Acceso denegado")));
    }

    // ==========================================
    // Test: Crear valoración en cita no finalizada
    // ==========================================
    @Test
    void testCrearValoracion_CitaNoFinalizada() throws Exception {
        // Arrange
        when(jwtUtil.extractId("token123")).thenReturn(1);
        doThrow(new RuntimeException("Solo puedes valorar citas que ya hayan sido finalizadas."))
                .when(valoracionService).crearValoracion(any(), anyInt());

        // Act & Assert
        mockMvc.perform(post("/api/valoraciones")
                .header("Authorization", "Bearer token123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(valoracionDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("finalizadas")));
    }

    // ==========================================
    // Test: Crear valoración duplicada
    // ==========================================
    @Test
    void testCrearValoracion_Duplicada() throws Exception {
        // Arrange
        when(jwtUtil.extractId("token123")).thenReturn(1);
        doThrow(new RuntimeException("Ya has enviado una valoración para este servicio."))
                .when(valoracionService).crearValoracion(any(), anyInt());

        // Act & Assert
        mockMvc.perform(post("/api/valoraciones")
                .header("Authorization", "Bearer token123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(valoracionDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("Ya has enviado")));
    }

    // ==========================================
    // Test: Crear valoración sin token
    // ==========================================
    @Test
    void testCrearValoracion_SinToken() throws Exception {
        // Arrange

        // Act & Assert
        mockMvc.perform(post("/api/valoraciones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(valoracionDTO)))
                .andExpect(status().isBadRequest());
    }

    // ==========================================
    // Test: Crear valoración con comentario vacío
    // ==========================================
    @Test
    void testCrearValoracion_ConComentarioVacio() throws Exception {
        // Arrange
        valoracionDTO.setComentario("");
        when(jwtUtil.extractId("token123")).thenReturn(1);
        doNothing().when(valoracionService).crearValoracion(any(), anyInt());

        // Act & Assert
        mockMvc.perform(post("/api/valoraciones")
                .header("Authorization", "Bearer token123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(valoracionDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("La valoración ha sido registrada con éxito."));

        verify(valoracionService, times(1)).crearValoracion(any(), anyInt());
    }

    // ==========================================
    // Test: Crear valoración con voto mínimo (1)
    // ==========================================
    @Test
    void testCrearValoracion_VotoMinimo() throws Exception {
        // Arrange
        valoracionDTO.setVoto((short) 1);
        when(jwtUtil.extractId("token123")).thenReturn(1);
        doNothing().when(valoracionService).crearValoracion(any(), anyInt());

        // Act & Assert
        mockMvc.perform(post("/api/valoraciones")
                .header("Authorization", "Bearer token123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(valoracionDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("La valoración ha sido registrada con éxito."));

        verify(valoracionService, times(1)).crearValoracion(any(), anyInt());
    }

    // ==========================================
    // Test: Crear valoración con voto máximo (5)
    // ==========================================
    @Test
    void testCrearValoracion_VotoMaximo() throws Exception {
        // Arrange
        valoracionDTO.setVoto((short) 5);
        when(jwtUtil.extractId("token123")).thenReturn(1);
        doNothing().when(valoracionService).crearValoracion(any(), anyInt());

        // Act & Assert
        mockMvc.perform(post("/api/valoraciones")
                .header("Authorization", "Bearer token123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(valoracionDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("La valoración ha sido registrada con éxito."));

        verify(valoracionService, times(1)).crearValoracion(any(), anyInt());
    }

    // ==========================================
    // Test: Obtener valoraciones por trabajador
    // ==========================================
    @Test
    void testObtenerPorTrabajador_Exitoso() throws Exception {
        when(jwtUtil.extractId("token123")).thenReturn(1);
        when(valoracionService.obtenerPorTrabajador(eq(2))).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/valoraciones/trabajador/{idTrabajador}", 2)
                .header("Authorization", "Bearer token123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].idCita").value(1));

        verify(valoracionService, times(1)).obtenerPorTrabajador(2);
    }

    @Test
    void testObtenerPorTrabajador_SinToken() throws Exception {
        mockMvc.perform(get("/api/valoraciones/trabajador/{idTrabajador}", 2))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Token de autorización no encontrado."));
    }

    // ==========================================
    // Test: Obtener valoraciones por cliente
    // ==========================================
    @Test
    void testObtenerPorCliente_Exitoso() throws Exception {
        when(jwtUtil.extractId("token123")).thenReturn(10);
        when(valoracionService.obtenerPorCliente(eq(10))).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/valoraciones/cliente/{idCliente}", 10)
                .header("Authorization", "Bearer token123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].idCliente").value(1));

        verify(valoracionService, times(1)).obtenerPorCliente(10);
    }

    @Test
    void testObtenerPorCliente_AccesoDenegado() throws Exception {
        when(jwtUtil.extractId("token123")).thenReturn(11);

        mockMvc.perform(get("/api/valoraciones/cliente/{idCliente}", 10)
                .header("Authorization", "Bearer token123"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("No tienes permiso para consultar estas valoraciones."));
    }

    @Test
    void testObtenerPorCliente_SinToken() throws Exception {
        mockMvc.perform(get("/api/valoraciones/cliente/{idCliente}", 10))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Token de autorización no encontrado."));
    }
}
