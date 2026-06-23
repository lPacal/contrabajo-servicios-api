package com.contrabajo.servicios_api.controller;

import com.contrabajo.servicios_api.dto.EstadoDTO;
import com.contrabajo.servicios_api.service.EstadoCatalogoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class EstadoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private EstadoCatalogoService estadoCatalogoService;

    @InjectMocks
    private EstadoController estadoController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(estadoController).build();
    }

    @Test
    void testListarEstados_Exitoso() throws Exception {
        EstadoDTO estado1 = new EstadoDTO(401, "CITA_PENDIENTE", "Pendiente", "Solicitud recibida");
        EstadoDTO estado2 = new EstadoDTO(402, "CITA_HANDSHAKE", "Handshake", "Aceptada por trabajador");

        when(estadoCatalogoService.listarEstados()).thenReturn(List.of(estado1, estado2));

        mockMvc.perform(get("/api/estados"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].codigo").value("CITA_PENDIENTE"))
                .andExpect(jsonPath("$[1].nombre").value("Handshake"));
    }
}
