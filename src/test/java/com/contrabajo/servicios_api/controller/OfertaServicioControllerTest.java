package com.contrabajo.servicios_api.controller;

import com.contrabajo.servicios_api.dto.OfertaServicioCreateDTO;
import com.contrabajo.servicios_api.dto.OfertaServicioResponseDTO;
import com.contrabajo.servicios_api.dto.OfertaServicioUpdateDTO;
import com.contrabajo.servicios_api.service.OfertaServicioService;
import com.contrabajo.servicios_api.utils.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class OfertaServicioControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OfertaServicioService ofertaService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private OfertaServicioController ofertaController;

    private ObjectMapper objectMapper;
    private OfertaServicioCreateDTO crearDTO;
    private OfertaServicioUpdateDTO actualizarDTO;
    private OfertaServicioResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(ofertaController).build();
        objectMapper = new ObjectMapper();

        crearDTO = new OfertaServicioCreateDTO();
        crearDTO.setTitulo("Reparación de electrodomésticos");
        crearDTO.setDescripcion("Servicio profesional");
        crearDTO.setPrecio(BigDecimal.valueOf(50000));
        crearDTO.setIdCategoria(1);
        crearDTO.setIdTipoPrecio(1);

        actualizarDTO = new OfertaServicioUpdateDTO();
        actualizarDTO.setTitulo("Titulo actualizado");

        responseDTO = new OfertaServicioResponseDTO();
        responseDTO.setId(1);
        responseDTO.setTitulo("Reparación de electrodomésticos");
        responseDTO.setDescripcion("Servicio profesional");
        responseDTO.setPrecio(BigDecimal.valueOf(50000));
    }

    // ==========================================
    // Test: Crear oferta exitosamente
    // ==========================================
    @Test
    void testCrearOferta_Exitoso() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer token123");
        when(jwtUtil.extractId("token123")).thenReturn(1);
        when(jwtUtil.extractRol("token123")).thenReturn("TRABAJADOR");
        // Nueva firma: crear(dto, idUsuario, rol, authorizationHeader)
        when(ofertaService.crear(any(), anyInt(), anyString(), anyString())).thenReturn(responseDTO);

        mockMvc.perform(post("/api/ofertas")
                .header("Authorization", "Bearer token123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(crearDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.titulo").value("Reparación de electrodomésticos"));

        verify(ofertaService, times(1)).crear(any(), anyInt(), anyString(), anyString());
    }

    // ==========================================
    // Test: Crear oferta con error
    // ==========================================
    @Test
    void testCrearOferta_Error() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer token123");
        when(jwtUtil.extractId("token123")).thenReturn(1);
        when(jwtUtil.extractRol("token123")).thenReturn("TRABAJADOR");
        when(ofertaService.crear(any(), anyInt(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Límite de ofertas alcanzado"));

        mockMvc.perform(post("/api/ofertas")
                .header("Authorization", "Bearer token123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(crearDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Límite de ofertas alcanzado"));
    }

    // ==========================================
    // Test: Listar todas las ofertas
    // ==========================================
    @Test
    void testListarTodas_Exitoso() throws Exception {
        List<OfertaServicioResponseDTO> ofertas = List.of(responseDTO);
        // Nueva firma: listarTodas(authorizationHeader)
        when(ofertaService.listarTodas(any())).thenReturn(ofertas);

        mockMvc.perform(get("/api/ofertas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titulo").value("Reparación de electrodomésticos"))
                .andExpect(jsonPath("$[0].id").value(1));

        verify(ofertaService, times(1)).listarTodas(any());
    }

    // ==========================================
    // Test: Listar todas sin ofertas
    // ==========================================
    @Test
    void testListarTodas_ListaVacia() throws Exception {
        when(ofertaService.listarTodas(any())).thenReturn(List.of());

        mockMvc.perform(get("/api/ofertas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ==========================================
    // Test: Buscar oferta por ID
    // ==========================================
    @Test
    void testBuscarPorId_Encontrada() throws Exception {
        // Nueva firma: buscarPorId(id, authorizationHeader)
        when(ofertaService.buscarPorId(eq(1), any())).thenReturn(responseDTO);

        mockMvc.perform(get("/api/ofertas/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.titulo").value("Reparación de electrodomésticos"));

        verify(ofertaService, times(1)).buscarPorId(eq(1), any());
    }

    // ==========================================
    // Test: Buscar oferta no encontrada
    // ==========================================
    @Test
    void testBuscarPorId_NoEncontrada() throws Exception {
        when(ofertaService.buscarPorId(eq(999), any()))
                .thenThrow(new RuntimeException("Oferta de servicio no encontrada"));

        mockMvc.perform(get("/api/ofertas/{id}", 999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Oferta de servicio no encontrada"));
    }

    // ==========================================
    // Test: Obtener disponibilidad de oferta
    // ==========================================
    @Test
    void testObtenerDisponibilidad_Exitoso() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer token123");
        when(jwtUtil.extractId("token123")).thenReturn(1);
        when(ofertaService.obtenerDisponibilidad(eq(1), anyInt())).thenReturn(true);

        mockMvc.perform(get("/api/ofertas/{id}/disponibilidad", 1)
                .header("Authorization", "Bearer token123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));

        verify(ofertaService, times(1)).obtenerDisponibilidad(1, 1);
    }

    @Test
    void testObtenerDisponibilidad_Error() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer token123");
        when(jwtUtil.extractId("token123")).thenReturn(1);
        when(ofertaService.obtenerDisponibilidad(anyInt(), anyInt()))
                .thenThrow(new RuntimeException("Oferta no encontrada"));

        mockMvc.perform(get("/api/ofertas/{id}/disponibilidad", 1)
                .header("Authorization", "Bearer token123"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value(false));
    }

    // ==========================================
    // Test: Activar disponibilidad de oferta
    // ==========================================
    @Test
    void testActivarDisponibilidad_Exitoso() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer token123");
        when(jwtUtil.extractId("token123")).thenReturn(1);
        when(jwtUtil.extractRol("token123")).thenReturn("TRABAJADOR");
        when(ofertaService.activarDisponibilidad(anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(responseDTO);

        mockMvc.perform(patch("/api/ofertas/{id}/disponibilidad/activar", 1)
                .header("Authorization", "Bearer token123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(ofertaService, times(1)).activarDisponibilidad(1, 1, "TRABAJADOR", "Bearer token123");
    }

    @Test
    void testActivarDisponibilidad_Error() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer token123");
        when(jwtUtil.extractId("token123")).thenReturn(1);
        when(jwtUtil.extractRol("token123")).thenReturn("TRABAJADOR");
        when(ofertaService.activarDisponibilidad(anyInt(), anyInt(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Oferta no encontrada"));

        mockMvc.perform(patch("/api/ofertas/{id}/disponibilidad/activar", 1)
                .header("Authorization", "Bearer token123"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Oferta no encontrada"));
    }

    // ==========================================
    // Test: Desactivar disponibilidad de oferta
    // ==========================================
    @Test
    void testDesactivarDisponibilidad_Exitoso() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer token123");
        when(jwtUtil.extractId("token123")).thenReturn(1);
        when(jwtUtil.extractRol("token123")).thenReturn("TRABAJADOR");
        when(ofertaService.desactivarDisponibilidad(anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(responseDTO);

        mockMvc.perform(patch("/api/ofertas/{id}/disponibilidad/desactivar", 1)
                .header("Authorization", "Bearer token123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(ofertaService, times(1)).desactivarDisponibilidad(1, 1, "TRABAJADOR", "Bearer token123");
    }

    @Test
    void testDesactivarDisponibilidad_Error() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer token123");
        when(jwtUtil.extractId("token123")).thenReturn(1);
        when(jwtUtil.extractRol("token123")).thenReturn("TRABAJADOR");
        when(ofertaService.desactivarDisponibilidad(anyInt(), anyInt(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Oferta no encontrada"));

        mockMvc.perform(patch("/api/ofertas/{id}/disponibilidad/desactivar", 1)
                .header("Authorization", "Bearer token123"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Oferta no encontrada"));
    }

    // ==========================================
    // Test: Actualizar oferta con PUT
    // ==========================================
    @Test
    void testActualizarOfertaPut_Exitoso() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer token123");
        when(jwtUtil.extractId("token123")).thenReturn(1);
        when(ofertaService.actualizar(anyInt(), any(), anyInt(), anyString())).thenReturn(responseDTO);

        mockMvc.perform(put("/api/ofertas/{id}", 1)
                .header("Authorization", "Bearer token123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(actualizarDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(ofertaService, times(1)).actualizar(anyInt(), any(), anyInt(), anyString());
    }

    @Test
    void testActualizarOfertaPut_Error() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer token123");
        when(jwtUtil.extractId("token123")).thenReturn(1);
        when(ofertaService.actualizar(anyInt(), any(), anyInt(), anyString()))
                .thenThrow(new RuntimeException("Acceso denegado"));

        mockMvc.perform(put("/api/ofertas/{id}", 1)
                .header("Authorization", "Bearer token123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(actualizarDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Acceso denegado"));
    }

    // ==========================================
    // Test: Listar ofertas por trabajador
    // ==========================================
    @Test
    void testListarPorTrabajador_Exitoso() throws Exception {
        List<OfertaServicioResponseDTO> ofertas = List.of(responseDTO);
        // Nueva firma: listarPorTrabajador(idTrabajador, authorizationHeader)
        when(ofertaService.listarPorTrabajador(eq(1), any())).thenReturn(ofertas);

        mockMvc.perform(get("/api/ofertas/trabajador/{idTrabajador}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titulo").value("Reparación de electrodomésticos"));

        verify(ofertaService, times(1)).listarPorTrabajador(eq(1), any());
    }

    // ==========================================
    // Test: Listar ofertas por trabajador sin ofertas
    // ==========================================
    @Test
    void testListarPorTrabajador_SinOfertas() throws Exception {
        when(ofertaService.listarPorTrabajador(eq(1), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/ofertas/trabajador/{idTrabajador}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ==========================================
    // Test: Actualizar oferta exitosamente
    // ==========================================
    @Test
    void testActualizarOferta_Exitoso() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer token123");
        when(jwtUtil.extractId("token123")).thenReturn(1);
        // Nueva firma: actualizar(id, dto, idUsuario, authorizationHeader)
        when(ofertaService.actualizar(anyInt(), any(), anyInt(), anyString())).thenReturn(responseDTO);

        mockMvc.perform(patch("/api/ofertas/{id}", 1)
                .header("Authorization", "Bearer token123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(actualizarDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(ofertaService, times(1)).actualizar(anyInt(), any(), anyInt(), anyString());
    }

    // ==========================================
    // Test: Actualizar oferta con error
    // ==========================================
    @Test
    void testActualizarOferta_Error() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer token123");
        when(jwtUtil.extractId("token123")).thenReturn(1);
        when(ofertaService.actualizar(anyInt(), any(), anyInt(), anyString()))
                .thenThrow(new RuntimeException("Acceso denegado"));

        mockMvc.perform(patch("/api/ofertas/{id}", 1)
                .header("Authorization", "Bearer token123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(actualizarDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Acceso denegado"));
    }

    // ==========================================
    // Test: Eliminar oferta exitosamente
    // ==========================================
    @Test
    void testEliminarOferta_Exitoso() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer token123");
        when(jwtUtil.extractId("token123")).thenReturn(1);
        when(jwtUtil.extractRol("token123")).thenReturn("TRABAJADOR");
        // Nueva firma: eliminar(id, idUsuario, rolUsuario)
        doNothing().when(ofertaService).eliminar(anyInt(), anyInt(), anyString());

        mockMvc.perform(delete("/api/ofertas/{id}", 1)
                .header("Authorization", "Bearer token123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Oferta eliminada correctamente."));

        verify(ofertaService, times(1)).eliminar(anyInt(), anyInt(), anyString());
    }

    // ==========================================
    // Test: Eliminar oferta con error
    // ==========================================
    @Test
    void testEliminarOferta_Error() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer token123");
        when(jwtUtil.extractId("token123")).thenReturn(1);
        when(jwtUtil.extractRol("token123")).thenReturn("TRABAJADOR");
        doThrow(new RuntimeException("Oferta no encontrada"))
                .when(ofertaService).eliminar(anyInt(), anyInt(), anyString());

        mockMvc.perform(delete("/api/ofertas/{id}", 1)
                .header("Authorization", "Bearer token123"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Oferta no encontrada"));
    }

    // ==========================================
    // Test: Crear oferta sin token
    // ==========================================
    @Test
    void testCrearOferta_SinToken() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        mockMvc.perform(post("/api/ofertas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(crearDTO)))
                .andExpect(status().isBadRequest());
    }

    // ==========================================
    // Test: Actualizar oferta sin token
    // ==========================================
    @Test
    void testActualizarOferta_SinToken() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        mockMvc.perform(patch("/api/ofertas/{id}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(actualizarDTO)))
                .andExpect(status().isBadRequest());
    }
}
