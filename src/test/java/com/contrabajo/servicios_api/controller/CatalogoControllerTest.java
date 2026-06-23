package com.contrabajo.servicios_api.controller;

import com.contrabajo.servicios_api.model.CategoriaServicio;
import com.contrabajo.servicios_api.model.TipoPrecio;
import com.contrabajo.servicios_api.repository.CategoriaServicioRepository;
import com.contrabajo.servicios_api.repository.TipoPrecioRepository;
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
class CatalogoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CategoriaServicioRepository categoriaRepository;

    @Mock
    private TipoPrecioRepository tipoPrecioRepository;

    @InjectMocks
    private CatalogoController catalogoController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(catalogoController).build();
    }

    @Test
    void testListarCategorias_Exitoso() throws Exception {
        CategoriaServicio categoria1 = new CategoriaServicio();
        categoria1.setId(1);
        categoria1.setNombre("Reparación");

        CategoriaServicio categoria2 = new CategoriaServicio();
        categoria2.setId(2);
        categoria2.setNombre("Limpieza");

        when(categoriaRepository.findAll()).thenReturn(List.of(categoria1, categoria2));

        mockMvc.perform(get("/api/catalogos/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Reparación"));
    }

    @Test
    void testListarTiposPrecio_Exitoso() throws Exception {
        TipoPrecio tipo1 = new TipoPrecio();
        tipo1.setId(1);
        tipo1.setNombre("Por hora");

        TipoPrecio tipo2 = new TipoPrecio();
        tipo2.setId(2);
        tipo2.setNombre("Por servicio");

        when(tipoPrecioRepository.findAll()).thenReturn(List.of(tipo1, tipo2));

        mockMvc.perform(get("/api/catalogos/tipos-precio"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].nombre").value("Por servicio"));
    }
}
