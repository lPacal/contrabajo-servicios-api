package com.contrabajo.servicios_api.service;

import com.contrabajo.servicios_api.dto.FotoResponseDTO;
import com.contrabajo.servicios_api.model.Foto;
import com.contrabajo.servicios_api.model.OfertaServicio;
import com.contrabajo.servicios_api.repository.FotoRepository;
import com.contrabajo.servicios_api.repository.OfertaServicioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FotoServiceTest {

    @Mock
    private FotoRepository fotoRepository;

    @Mock
    private OfertaServicioRepository ofertaRepository;

    @InjectMocks
    private FotoService fotoService;

    private OfertaServicio oferta;
    private Foto fotoExistente1;
    private Foto fotoExistente2;
    private Integer idOferta = 1;
    private Integer idUsuario = 2;

    @BeforeEach
    void setUp() {
        oferta = new OfertaServicio();
        oferta.setId(idOferta);
        oferta.setIdTrabajador(idUsuario);

        fotoExistente1 = new Foto();
        fotoExistente1.setId(1);
        fotoExistente1.setEnlace("https://cloudinary.com/example1.jpg");
        fotoExistente1.setOfertaServicio(oferta);
        fotoExistente1.setIdUsuario(idUsuario);

        fotoExistente2 = new Foto();
        fotoExistente2.setId(2);
        fotoExistente2.setEnlace("https://cloudinary.com/example2.jpg");
        fotoExistente2.setOfertaServicio(oferta);
        fotoExistente2.setIdUsuario(idUsuario);
    }

    @Test
    void testGuardarUrl_CreaNuevaFoto() {
        when(ofertaRepository.findById(idOferta)).thenReturn(Optional.of(oferta));
        when(fotoRepository.findByOfertaServicioId(idOferta)).thenReturn(List.of());
        when(fotoRepository.save(any(Foto.class))).thenAnswer(invocation -> {
            Foto foto = invocation.getArgument(0);
            foto.setId(10);
            return foto;
        });

        FotoResponseDTO resultado = fotoService.guardarUrl("https://cloudinary.com/nueva.jpg", idOferta, idUsuario);

        assertNotNull(resultado);
        assertEquals(10, resultado.getIdFoto());
        assertEquals("https://cloudinary.com/nueva.jpg", resultado.getEnlace());
        assertEquals(idOferta, resultado.getIdOfertaServicio());
        assertEquals(idUsuario, resultado.getIdUsuario());
        verify(fotoRepository, times(1)).save(any(Foto.class));
    }

    @Test
    void testGuardarUrl_ActualizaFotoExistenteYEliminaDuplicados() {
        when(ofertaRepository.findById(idOferta)).thenReturn(Optional.of(oferta));
        when(fotoRepository.findByOfertaServicioId(idOferta)).thenReturn(List.of(fotoExistente1, fotoExistente2));
        when(fotoRepository.save(any(Foto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FotoResponseDTO resultado = fotoService.guardarUrl("https://cloudinary.com/actualizada.jpg", idOferta, idUsuario);

        assertNotNull(resultado);
        assertEquals(fotoExistente1.getId(), resultado.getIdFoto());
        assertEquals("https://cloudinary.com/actualizada.jpg", resultado.getEnlace());
        verify(fotoRepository, times(1)).save(fotoExistente1);
        verify(fotoRepository, times(1)).delete(fotoExistente2);
    }

    @Test
    void testGuardarUrl_OfertaNoEncontrada() {
        when(ofertaRepository.findById(idOferta)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> fotoService.guardarUrl("https://cloudinary.com/nueva.jpg", idOferta, idUsuario));
        assertTrue(ex.getMessage().contains("Oferta no encontrada."));
        verify(fotoRepository, never()).save(any());
    }

    @Test
    void testGuardarUrl_PermisoDenegado() {
        OfertaServicio ofertaAjena = new OfertaServicio();
        ofertaAjena.setId(idOferta);
        ofertaAjena.setIdTrabajador(999);
        when(ofertaRepository.findById(idOferta)).thenReturn(Optional.of(ofertaAjena));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> fotoService.guardarUrl("https://cloudinary.com/nueva.jpg", idOferta, idUsuario));
        assertTrue(ex.getMessage().contains("No tienes permiso"));
        verify(fotoRepository, never()).save(any());
    }

    @Test
    void testListarPorOferta_DevuelveFotos() {
        when(fotoRepository.findByOfertaServicioId(idOferta)).thenReturn(List.of(fotoExistente1, fotoExistente2));

        List<FotoResponseDTO> resultado = fotoService.listarPorOferta(idOferta);

        assertEquals(2, resultado.size());
        assertEquals(fotoExistente1.getId(), resultado.get(0).getIdFoto());
        assertEquals(fotoExistente2.getEnlace(), resultado.get(1).getEnlace());
    }

    @Test
    void testEliminar_Exitoso() {
        Foto foto = new Foto();
        foto.setId(5);
        foto.setIdUsuario(idUsuario);
        when(fotoRepository.findById(5)).thenReturn(Optional.of(foto));

        fotoService.eliminar(5, idUsuario);

        verify(fotoRepository, times(1)).delete(foto);
    }

    @Test
    void testEliminar_FotoNoEncontrada() {
        when(fotoRepository.findById(99)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> fotoService.eliminar(99, idUsuario));
        assertTrue(ex.getMessage().contains("Foto no encontrada."));
        verify(fotoRepository, never()).delete(any());
    }

    @Test
    void testEliminar_PermisoDenegado() {
        Foto foto = new Foto();
        foto.setId(5);
        foto.setIdUsuario(999);
        when(fotoRepository.findById(5)).thenReturn(Optional.of(foto));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> fotoService.eliminar(5, idUsuario));
        assertTrue(ex.getMessage().contains("No tienes permiso"));
        verify(fotoRepository, never()).delete(any());
    }
}
