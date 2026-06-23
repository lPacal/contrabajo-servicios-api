package com.contrabajo.servicios_api.controller;

import com.contrabajo.servicios_api.dto.FotoRequestDTO;
import com.contrabajo.servicios_api.dto.FotoResponseDTO;
import com.contrabajo.servicios_api.service.FotoService;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FotoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private FotoService fotoService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private FotoController fotoController;

    private ObjectMapper objectMapper;
    private FotoRequestDTO fotoRequestDTO;
    private FotoResponseDTO fotoResponseDTO;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(fotoController).build();
        objectMapper = new ObjectMapper();

        fotoRequestDTO = new FotoRequestDTO();
        fotoRequestDTO.setUrl("https://cloudinary.com/example.jpg");

        fotoResponseDTO = new FotoResponseDTO(1, "https://cloudinary.com/example.jpg", LocalDateTime.now(), 1, 2);
    }

    @Test
    void testRegistrarFoto_Exitoso() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer token123");
        when(jwtUtil.extractId("token123")).thenReturn(2);
        when(fotoService.guardarUrl(any(), anyInt(), anyInt())).thenReturn(fotoResponseDTO);

        mockMvc.perform(post("/api/fotos/{idOferta}", 1)
                .header("Authorization", "Bearer token123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(fotoRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id_foto").value(1))
                .andExpect(jsonPath("$.enlace").value("https://cloudinary.com/example.jpg"));
    }

    @Test
    void testRegistrarFoto_UrlObligatoria() throws Exception {
        fotoRequestDTO.setUrl("   ");

        mockMvc.perform(post("/api/fotos/{idOferta}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(fotoRequestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("La URL de la foto es obligatoria."));
    }

    @Test
    void testRegistrarFoto_ErrorDelServicio() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer token123");
        when(jwtUtil.extractId("token123")).thenReturn(2);
        when(fotoService.guardarUrl(any(), anyInt(), anyInt()))
                .thenThrow(new RuntimeException("No tienes permiso para subir fotos a esta oferta."));

        mockMvc.perform(post("/api/fotos/{idOferta}", 1)
                .header("Authorization", "Bearer token123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(fotoRequestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("No tienes permiso para subir fotos a esta oferta."));
    }

    @Test
    void testListarPorOferta_Exitoso() throws Exception {
        FotoResponseDTO segundaFoto = new FotoResponseDTO(2, "https://cloudinary.com/example2.jpg", LocalDateTime.now(), 1, 2);
        when(fotoService.listarPorOferta(1)).thenReturn(List.of(fotoResponseDTO, segundaFoto));

        mockMvc.perform(get("/api/fotos/oferta/{idOferta}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id_foto").value(1))
                .andExpect(jsonPath("$[1].id_foto").value(2));
    }

    @Test
    void testEliminar_Exitoso() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer token123");
        when(jwtUtil.extractId("token123")).thenReturn(2);
        doNothing().when(fotoService).eliminar(1, 2);

        mockMvc.perform(delete("/api/fotos/{idFoto}", 1)
                .header("Authorization", "Bearer token123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").exists());
    }

    @Test
    void testEliminar_ErrorDelServicio() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer token123");
        when(jwtUtil.extractId("token123")).thenReturn(2);
        doThrow(new RuntimeException("No tienes permiso para eliminar esta foto."))
                .when(fotoService).eliminar(1, 2);

        mockMvc.perform(delete("/api/fotos/{idFoto}", 1)
                .header("Authorization", "Bearer token123"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("No tienes permiso para eliminar esta foto."));
    }
}
