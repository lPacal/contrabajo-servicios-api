package com.contrabajo.servicios_api.service;

import com.contrabajo.servicios_api.dto.EstadoDTO;
import com.contrabajo.servicios_api.model.Estado;
import com.contrabajo.servicios_api.repository.EstadoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EstadoCatalogoServiceTest {

    @Mock
    private EstadoRepository estadoRepository;

    @InjectMocks
    private EstadoCatalogoService estadoCatalogoService;

    private Estado estado1;
    private Estado estado2;

    @BeforeEach
    void setUp() {
        estado1 = new Estado();
        estado1.setId((short) 2);
        estado1.setCodigo("CITA_HANDSHAKE");
        estado1.setNombre("Handshake");
        estado1.setDescripcion("Aceptado por trabajador");

        estado2 = new Estado();
        estado2.setId((short) 1);
        estado2.setCodigo("CITA_PENDIENTE");
        estado2.setNombre("Pendiente");
        estado2.setDescripcion("Solicitud recibida");
    }

    @Test
    void testListarEstados_OrdenadoYTransformado() {
        when(estadoRepository.findAll(org.mockito.ArgumentMatchers.any(org.springframework.data.domain.Sort.class)))
                .thenReturn(List.of(estado1, estado2));

        List<EstadoDTO> resultado = estadoCatalogoService.listarEstados();

        assertEquals(2, resultado.size());
        assertEquals("CITA_HANDSHAKE", resultado.get(0).getCodigo());
        assertEquals("Handshake", resultado.get(0).getNombre());
        assertEquals("CITA_PENDIENTE", resultado.get(1).getCodigo());
    }
}
