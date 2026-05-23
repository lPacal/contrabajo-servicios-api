package com.contrabajo.servicios_api.service;

import com.contrabajo.servicios_api.dto.OfertaServicioCreateDTO;
import com.contrabajo.servicios_api.dto.OfertaServicioResponseDTO;
import com.contrabajo.servicios_api.dto.OfertaServicioUpdateDTO;
import com.contrabajo.servicios_api.model.CategoriaServicio;
import com.contrabajo.servicios_api.model.OfertaServicio;
import com.contrabajo.servicios_api.model.TipoPrecio;
import com.contrabajo.servicios_api.repository.CategoriaServicioRepository;
import com.contrabajo.servicios_api.repository.OfertaServicioRepository;
import com.contrabajo.servicios_api.repository.TipoPrecioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OfertaServicioServiceTest {

    @Mock
    private OfertaServicioRepository ofertaRepository;

    @Mock
    private CategoriaServicioRepository categoriaRepository;

    @Mock
    private TipoPrecioRepository tipoPrecioRepository;

    @InjectMocks
    private OfertaServicioService ofertaService;

    private OfertaServicioCreateDTO crearDTO;
    private OfertaServicioUpdateDTO actualizarDTO;
    private OfertaServicio ofertaMock;
    private CategoriaServicio categoriaMock;
    private TipoPrecio tipoPrecioMock;
    private Integer idTrabajador = 1;
    private final String authorizationHeader = null;

    @BeforeEach
    void setUp() {
        // Setup categoría
        categoriaMock = new CategoriaServicio();
        categoriaMock.setId(1);
        categoriaMock.setNombre("Reparación");

        // Setup tipo precio
        tipoPrecioMock = new TipoPrecio();
        tipoPrecioMock.setId(1);
        tipoPrecioMock.setNombre("Por hora");

        // Setup oferta
        ofertaMock = new OfertaServicio();
        ofertaMock.setId(1);
        ofertaMock.setTitulo("Reparación de electrodomésticos");
        ofertaMock.setDescripcion("Servicio profesional de reparación");
        ofertaMock.setPrecio(BigDecimal.valueOf(50000));
        ofertaMock.setDisponible(true);
        ofertaMock.setBorrado(false);
        ofertaMock.setIdTrabajador(idTrabajador);
        ofertaMock.setCategoriaServicio(categoriaMock);
        ofertaMock.setTipoPrecio(tipoPrecioMock);
        ofertaMock.setFechaPublicacion(LocalDateTime.now());

        // Setup DTO crear
        crearDTO = new OfertaServicioCreateDTO();
        crearDTO.setTitulo("Reparación de electrodomésticos");
        crearDTO.setDescripcion("Servicio profesional de reparación");
        crearDTO.setPrecio(BigDecimal.valueOf(50000));
        crearDTO.setIdCategoria(1);
        crearDTO.setIdTipoPrecio(1);

        // Setup DTO actualizar
        actualizarDTO = new OfertaServicioUpdateDTO();
        actualizarDTO.setTitulo("Titulo actualizado");
    }

    // ==========================================
    // Test: Crear oferta exitosamente
    // ==========================================
    @Test
    void testCrear_Exitoso() {
        // Arrange
        when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoriaMock));
        when(tipoPrecioRepository.findById(1)).thenReturn(Optional.of(tipoPrecioMock));
        when(ofertaRepository.save(any(OfertaServicio.class))).thenReturn(ofertaMock);

        // Act
        OfertaServicioResponseDTO resultado = ofertaService.crear(crearDTO, idTrabajador, authorizationHeader);

        // Assert
        assertNotNull(resultado);
        assertEquals("Reparación de electrodomésticos", resultado.getTitulo());
        verify(ofertaRepository, times(1)).save(any(OfertaServicio.class));
    }

    // ==========================================
    // Test: Crear oferta con categoría
    // ==========================================
    @Test
    void testCrear_ConOfertasPrevias_Exitoso() {
        // Arrange
        when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoriaMock));
        when(tipoPrecioRepository.findById(1)).thenReturn(Optional.of(tipoPrecioMock));
        when(ofertaRepository.save(any(OfertaServicio.class))).thenReturn(ofertaMock);

        // Act
        OfertaServicioResponseDTO resultado = ofertaService.crear(crearDTO, idTrabajador, authorizationHeader);

        // Assert
        assertNotNull(resultado);
        verify(ofertaRepository, times(1)).save(any(OfertaServicio.class));
    }

    // ==========================================
    // Test: Crear con tipo de precio
    // ==========================================
    @Test
    void testCrear_ConOfertaAnterior_Exitoso() {
        // Arrange
        when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoriaMock));
        when(tipoPrecioRepository.findById(1)).thenReturn(Optional.of(tipoPrecioMock));
        when(ofertaRepository.save(any(OfertaServicio.class))).thenReturn(ofertaMock);

        // Act
        OfertaServicioResponseDTO resultado = ofertaService.crear(crearDTO, idTrabajador, authorizationHeader);

        // Assert
        assertNotNull(resultado);
        verify(ofertaRepository, times(1)).save(any(OfertaServicio.class));
    }

    // ==========================================
    // Test: Crear con categoría no existente
    // ==========================================
    @Test
    void testCrear_CategoriaNoExiste() {
        // Arrange
        // crearDTO tiene idCategoria=1, así que mockemos findById(1) retornando empty
        when(categoriaRepository.findById(1)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> ofertaService.crear(crearDTO, idTrabajador, authorizationHeader));
        assertTrue(exception.getMessage().contains("categoría") ||
                   exception.getMessage().contains("categor"));
    }

    // ==========================================
    // Test: Listar todas las ofertas
    // ==========================================
    @Test
    void testListarTodas_Exitoso() {
        // Arrange
        List<OfertaServicio> ofertas = List.of(ofertaMock);
        when(ofertaRepository.findAll()).thenReturn(ofertas);

        // Act
        List<OfertaServicioResponseDTO> resultado = ofertaService.listarTodas(authorizationHeader);

        // Assert
        assertEquals(1, resultado.size());
        assertEquals("Reparación de electrodomésticos", resultado.get(0).getTitulo());
        verify(ofertaRepository, times(1)).findAll();
    }

    // ==========================================
    // Test: Listar todas sin ofertas
    // ==========================================
    @Test
    void testListarTodas_ListaVacia() {
        // Arrange
        when(ofertaRepository.findAll()).thenReturn(new ArrayList<>());

        // Act
        List<OfertaServicioResponseDTO> resultado = ofertaService.listarTodas(authorizationHeader);

        // Assert
        assertTrue(resultado.isEmpty());
        verify(ofertaRepository, times(1)).findAll();
    }

    // ==========================================
    // Test: Buscar oferta por ID
    // ==========================================
    @Test
    void testBuscarPorId_Encontrada() {
        // Arrange
        when(ofertaRepository.findById(1)).thenReturn(Optional.of(ofertaMock));

        // Act
        OfertaServicioResponseDTO resultado = ofertaService.buscarPorId(1, authorizationHeader);

        // Assert
        assertNotNull(resultado);
        assertEquals("Reparación de electrodomésticos", resultado.getTitulo());
        verify(ofertaRepository, times(1)).findById(1);
    }

    // ==========================================
    // Test: Buscar oferta no encontrada
    // ==========================================
    @Test
    void testBuscarPorId_NoEncontrada() {
        // Arrange
        when(ofertaRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> ofertaService.buscarPorId(999, authorizationHeader));
        assertTrue(exception.getMessage().contains("no encontrada"));
    }

    // ==========================================
    // Test: Listar por trabajador
    // ==========================================
    @Test
    void testListarPorTrabajador_Exitoso() {
        // Arrange
        List<OfertaServicio> ofertas = List.of(ofertaMock);
        when(ofertaRepository.findByIdTrabajadorOrderByFechaPublicacionDesc(idTrabajador))
                .thenReturn(ofertas);

        // Act
        List<OfertaServicioResponseDTO> resultado = ofertaService.listarPorTrabajador(idTrabajador, authorizationHeader);

        // Assert
        assertEquals(1, resultado.size());
        verify(ofertaRepository, times(1)).findByIdTrabajadorOrderByFechaPublicacionDesc(idTrabajador);
    }

    // ==========================================
    // Test: Listar por trabajador sin ofertas
    // ==========================================
    @Test
    void testListarPorTrabajador_SinOfertas() {
        // Arrange
        when(ofertaRepository.findByIdTrabajadorOrderByFechaPublicacionDesc(idTrabajador))
                .thenReturn(new ArrayList<>());

        // Act
        List<OfertaServicioResponseDTO> resultado = ofertaService.listarPorTrabajador(idTrabajador, authorizationHeader);

        // Assert
        assertTrue(resultado.isEmpty());
    }

    // ==========================================
    // Test: Actualizar oferta exitosamente
    // ==========================================
    @Test
    void testActualizar_Exitoso() {
        // Arrange
        when(ofertaRepository.findById(1)).thenReturn(Optional.of(ofertaMock));
        when(ofertaRepository.save(any(OfertaServicio.class))).thenReturn(ofertaMock);

        // Act
        OfertaServicioResponseDTO resultado = ofertaService.actualizar(1, actualizarDTO, idTrabajador, authorizationHeader);

        // Assert
        assertNotNull(resultado);
        verify(ofertaRepository, times(1)).save(any(OfertaServicio.class));
    }

    // ==========================================
    // Test: Actualizar oferta no encontrada
    // ==========================================
    @Test
    void testActualizar_NoEncontrada() {
        // Arrange
        when(ofertaRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> ofertaService.actualizar(999, actualizarDTO, idTrabajador, authorizationHeader));
        assertTrue(exception.getMessage().contains("no encontrada"));
    }

    // ==========================================
    // Test: Actualizar oferta borrada
    // ==========================================
    @Test
    void testActualizar_OfertaBorrada() {
        // Arrange
        ofertaMock.setBorrado(true);
        when(ofertaRepository.findById(1)).thenReturn(Optional.of(ofertaMock));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> ofertaService.actualizar(1, actualizarDTO, idTrabajador, authorizationHeader));
        assertTrue(exception.getMessage().contains("eliminada"));
    }

    // ==========================================
    // Test: Actualizar sin permisos
    // ==========================================
    @Test
    void testActualizar_SinPermisos() {
        // Arrange
        when(ofertaRepository.findById(1)).thenReturn(Optional.of(ofertaMock));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> ofertaService.actualizar(1, actualizarDTO, 999, authorizationHeader)); // ID diferente
        assertTrue(exception.getMessage().contains("Acceso denegado"));
    }

    // ==========================================
    // Test: Actualizar activando oferta
    // ==========================================
    @Test
    void testActualizar_ActivandoOferta() {
        // Arrange
        ofertaMock.setDisponible(false);
        actualizarDTO.setDisponible(true);

        when(ofertaRepository.findById(1)).thenReturn(Optional.of(ofertaMock));
        when(ofertaRepository.save(any(OfertaServicio.class))).thenReturn(ofertaMock);

        // Act
        ofertaService.actualizar(1, actualizarDTO, idTrabajador, authorizationHeader);

        // Assert
        verify(ofertaRepository, times(1)).save(any(OfertaServicio.class));
    }

    // ==========================================
    // Test: Eliminar oferta
    // ==========================================
    @Test
    void testEliminar_Exitoso() {
        // Arrange
        when(ofertaRepository.findById(1)).thenReturn(Optional.of(ofertaMock));
        when(ofertaRepository.save(any(OfertaServicio.class))).thenReturn(ofertaMock);

        // Act
        ofertaService.eliminar(1, idTrabajador, null);

        // Assert
        verify(ofertaRepository, times(1)).save(any(OfertaServicio.class));
    }

    // ==========================================
    // Test: Eliminar oferta no encontrada
    // ==========================================
    @Test
    void testEliminar_NoEncontrada() {
        // Arrange
        when(ofertaRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> ofertaService.eliminar(999, idTrabajador, null));
        assertTrue(exception.getMessage().contains("no encontrada"));
    }

    // ==========================================
    // Test: Eliminar sin permisos
    // ==========================================
    @Test
    void testEliminar_SinPermisos() {
        // Arrange
        when(ofertaRepository.findById(1)).thenReturn(Optional.of(ofertaMock));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> ofertaService.eliminar(1, 999, null)); // ID diferente
        assertTrue(exception.getMessage().contains("Acceso denegado"));
    }
}