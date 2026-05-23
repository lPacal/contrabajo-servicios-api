package com.contrabajo.servicios_api.service;

import com.contrabajo.servicios_api.dto.ValoracionRequestDTO;
import com.contrabajo.servicios_api.dto.ValoracionResponseDTO;
import com.contrabajo.servicios_api.model.CitaServicio;
import com.contrabajo.servicios_api.model.Estado;
import com.contrabajo.servicios_api.model.OfertaServicio;
import com.contrabajo.servicios_api.model.Valoracion;
import com.contrabajo.servicios_api.repository.CitaServicioRepository;
import com.contrabajo.servicios_api.repository.ValoracionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ValoracionServiceTest {

    @Mock
    private ValoracionRepository valoracionRepository;

    @Mock
    private CitaServicioRepository citaRepository;

    @InjectMocks
    private ValoracionService valoracionService;

    private ValoracionRequestDTO valoracionRequestDTO;
    private Valoracion valoracionMock;
    private CitaServicio citaMock;
    private Estado estadoMock;
    private Integer idClienteAutenticado = 1;
    private Integer idTrabajador = 2;
    private Integer idCita = 1;

    @BeforeEach
    void setUp() {
        // Setup estado
        estadoMock = new Estado();
        estadoMock.setId((short) 1);
        estadoMock.setNombre("Finalizado");
        estadoMock.setCodigo("CITA_FINALIZADO");

        // Setup cita
        citaMock = new CitaServicio();
        citaMock.setId(idCita);
        citaMock.setIdCliente(idClienteAutenticado);
        citaMock.setIdTrabajador(idTrabajador);
        citaMock.setEstado(estadoMock);
        citaMock.setFechaSolicitud(LocalDateTime.now().minusHours(2));
        OfertaServicio ofertaMock = new OfertaServicio();
        ofertaMock.setId(1);
        citaMock.setOfertaServicio(ofertaMock);

        // Setup valoración
        valoracionMock = new Valoracion();
        valoracionMock.setId((long) 1);
        valoracionMock.setCita(citaMock);
        valoracionMock.setIdCliente(idClienteAutenticado);
        valoracionMock.setIdTrabajador(idTrabajador);
        valoracionMock.setVoto((short) 5);
        valoracionMock.setComentario("Excelente servicio");
        valoracionMock.setFechaVoto(LocalDateTime.now());

        // Setup DTO
        valoracionRequestDTO = new ValoracionRequestDTO();
        valoracionRequestDTO.setIdCita(idCita);
        valoracionRequestDTO.setVoto((short) 5);
        valoracionRequestDTO.setComentario("Excelente servicio");
    }

    // ==========================================
    // Test: Crear valoración exitosa
    // ==========================================
    @Test
    void testCrearValoracion_Exitoso() {
        // Arrange
        // El servicio valida: voto OK → cita existe → acceso OK → estado FINALIZADO → NO duplicada
        when(citaRepository.findById(idCita)).thenReturn(Optional.of(citaMock));
        when(valoracionRepository.existsByCita(citaMock)).thenReturn(false); // no es duplicado

        // Act
        valoracionService.crearValoracion(valoracionRequestDTO, idClienteAutenticado);

        // Assert
        verify(valoracionRepository, times(1)).save(any(Valoracion.class));
    }

    // ==========================================
    // Test: Crear valoración con voto inválido (menor a 1)
    // ==========================================
    @Test
    void testCrearValoracion_VotoMenorA1() {
        // Arrange
        valoracionRequestDTO.setVoto((short) 0);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> valoracionService.crearValoracion(valoracionRequestDTO, idClienteAutenticado));
        assertTrue(exception.getMessage().contains("entre 1 y 5"));
        verify(valoracionRepository, never()).save(any());
    }

    // ==========================================
    // Test: Crear valoración con voto inválido (mayor a 5)
    // ==========================================
    @Test
    void testCrearValoracion_VotoMayorA5() {
        // Arrange
        valoracionRequestDTO.setVoto((short) 6);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> valoracionService.crearValoracion(valoracionRequestDTO, idClienteAutenticado));
        assertTrue(exception.getMessage().contains("entre 1 y 5"));
        verify(valoracionRepository, never()).save(any());
    }

    // ==========================================
    // Test: Crear valoración con voto nulo
    // ==========================================
    @Test
    void testCrearValoracion_VotoNulo() {
        // Arrange
        valoracionRequestDTO.setVoto(null);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> valoracionService.crearValoracion(valoracionRequestDTO, idClienteAutenticado));
        assertTrue(exception.getMessage().contains("entre 1 y 5"));
        verify(valoracionRepository, never()).save(any());
    }

    // ==========================================
    // Test: Crear valoración con cita no encontrada
    // ==========================================
    @Test
    void testCrearValoracion_CitaNoEncontrada() {
        // Arrange
        when(citaRepository.findById(999)).thenReturn(Optional.empty());
        valoracionRequestDTO.setIdCita(999);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> valoracionService.crearValoracion(valoracionRequestDTO, idClienteAutenticado));
        assertTrue(exception.getMessage().contains("Cita no encontrada"));
        verify(valoracionRepository, never()).save(any());
    }

    // ==========================================
    // Test: Crear valoración sin permisos
    // ==========================================
    @Test
    void testCrearValoracion_SinPermisos() {
        // Arrange
        when(citaRepository.findById(idCita)).thenReturn(Optional.of(citaMock));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> valoracionService.crearValoracion(valoracionRequestDTO, 999)); // ID diferente
        assertTrue(exception.getMessage().contains("Acceso denegado"));
        verify(valoracionRepository, never()).save(any());
    }

    // ==========================================
    // Test: Crear valoración en cita no finalizada
    // ==========================================
    @Test
    void testCrearValoracion_CitaNoFinalizada() {
        // Arrange
        Estado estadoPendiente = new Estado();
        estadoPendiente.setNombre("Pendiente");
        citaMock.setEstado(estadoPendiente);

        when(citaRepository.findById(idCita)).thenReturn(Optional.of(citaMock));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> valoracionService.crearValoracion(valoracionRequestDTO, idClienteAutenticado));
        assertTrue(exception.getMessage().contains("finalizadas"));
        verify(valoracionRepository, never()).save(any());
    }

    // ==========================================
    // Test: Crear valoración duplicada
    // ==========================================
    @Test
    void testCrearValoracion_Duplicada() {
        // Arrange
        // La cita debe estar finalizada para llegar a la verificación de duplicado
        // citaMock ya tiene estado "Finalizado" (configurado en setUp)
        when(citaRepository.findById(idCita)).thenReturn(Optional.of(citaMock));
        when(valoracionRepository.existsByCita(citaMock)).thenReturn(true); // ya existe

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> valoracionService.crearValoracion(valoracionRequestDTO, idClienteAutenticado));
        assertTrue(exception.getMessage().contains("ya has enviado") ||
                   exception.getMessage().contains("Ya has enviado"));
        verify(valoracionRepository, never()).save(any());
    }

    // ==========================================
    // Test: Obtener valoraciones por trabajador
    // ==========================================
    @Test
    void testObtenerPorTrabajador_Exitoso() {
        // Arrange
        List<Valoracion> valoraciones = List.of(valoracionMock);
        when(valoracionRepository.findByIdTrabajadorOrderByFechaVotoDesc(idTrabajador))
                .thenReturn(valoraciones);

        // Act
        List<ValoracionResponseDTO> resultado = valoracionService.obtenerPorTrabajador(idTrabajador);

        // Assert
        assertEquals(1, resultado.size());
        assertEquals((short) 5, resultado.get(0).getVoto());
        verify(valoracionRepository, times(1)).findByIdTrabajadorOrderByFechaVotoDesc(idTrabajador);
    }

    // ==========================================
    // Test: Obtener valoraciones por trabajador sin valoraciones
    // ==========================================
    @Test
    void testObtenerPorTrabajador_SinValoraciones() {
        // Arrange
        when(valoracionRepository.findByIdTrabajadorOrderByFechaVotoDesc(idTrabajador))
                .thenReturn(List.of());

        // Act
        List<ValoracionResponseDTO> resultado = valoracionService.obtenerPorTrabajador(idTrabajador);

        // Assert
        assertTrue(resultado.isEmpty());
    }

    // ==========================================
    // Test: Obtener valoraciones por cliente
    // ==========================================
    @Test
    void testObtenerPorCliente_Exitoso() {
        // Arrange
        List<Valoracion> valoraciones = List.of(valoracionMock);
        when(valoracionRepository.findByIdClienteOrderByFechaVotoDesc(idClienteAutenticado))
                .thenReturn(valoraciones);

        // Act
        List<ValoracionResponseDTO> resultado = valoracionService.obtenerPorCliente(idClienteAutenticado);

        // Assert
        assertEquals(1, resultado.size());
        assertEquals(idTrabajador, resultado.get(0).getIdTrabajador());
        verify(valoracionRepository, times(1)).findByIdClienteOrderByFechaVotoDesc(idClienteAutenticado);
    }

    // ==========================================
    // Test: Obtener valoraciones por cliente sin valoraciones
    // ==========================================
    @Test
    void testObtenerPorCliente_SinValoraciones() {
        // Arrange
        when(valoracionRepository.findByIdClienteOrderByFechaVotoDesc(idClienteAutenticado))
                .thenReturn(List.of());

        // Act
        List<ValoracionResponseDTO> resultado = valoracionService.obtenerPorCliente(idClienteAutenticado);

        // Assert
        assertTrue(resultado.isEmpty());
    }

    // ==========================================
    // Test: Conversión de Valoración a DTO
    // ==========================================
    @Test
    void testConversionADto_Correcta() {
        // Arrange
        List<Valoracion> valoraciones = List.of(valoracionMock);
        when(valoracionRepository.findByIdTrabajadorOrderByFechaVotoDesc(idTrabajador))
                .thenReturn(valoraciones);

        // Act
        List<ValoracionResponseDTO> resultado = valoracionService.obtenerPorTrabajador(idTrabajador);

        // Assert
        ValoracionResponseDTO dto = resultado.get(0);
        assertEquals(1, dto.getId());
        assertEquals(idCita, dto.getIdCita());
        assertEquals(idClienteAutenticado, dto.getIdCliente());
        assertEquals(idTrabajador, dto.getIdTrabajador());
        assertEquals("Excelente servicio", dto.getComentario());
    }
}
