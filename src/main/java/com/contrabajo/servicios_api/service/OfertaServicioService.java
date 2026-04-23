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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@org.hibernate.annotations.SQLRestriction("borrado = 0")
public class OfertaServicioService {

    private final OfertaServicioRepository ofertaRepository;
    private final CategoriaServicioRepository categoriaRepository;
    private final TipoPrecioRepository tipoPrecioRepository;

    // ==========================================
    // 1. CREAR
    // ==========================================
    @Transactional
    public OfertaServicioResponseDTO crear(OfertaServicioCreateDTO dto, Integer idUsuarioAutenticado) {
        
        OfertaServicio nuevaOferta = new OfertaServicio();
        nuevaOferta.setTitulo(dto.getTitulo());
        nuevaOferta.setDescripcion(dto.getDescripcion());
        nuevaOferta.setPrecio(dto.getPrecio());
        
        // La oferta nace disponible por defecto
        nuevaOferta.setDisponible(true);
        nuevaOferta.setBorrado(false);
        nuevaOferta.setIdTrabajador(idUsuarioAutenticado);

        if (dto.getIdCategoria() != null) {
            CategoriaServicio cat = categoriaRepository.findById(dto.getIdCategoria())
                    .orElseThrow(() -> new RuntimeException("La categoría indicada no existe."));
            nuevaOferta.setCategoriaServicio(cat);
        }

        if (dto.getIdTipoPrecio() != null) {
            TipoPrecio tp = tipoPrecioRepository.findById(dto.getIdTipoPrecio())
                    .orElseThrow(() -> new RuntimeException("El tipo de precio indicado no existe."));
            nuevaOferta.setTipoPrecio(tp);
        }

        OfertaServicio guardada = ofertaRepository.save(nuevaOferta);
        return convertirADto(guardada);
    }

    // ==========================================
    // 2. LEER
    // ==========================================
    @Transactional(readOnly = true)
    public List<OfertaServicioResponseDTO> listarTodas() {
        return ofertaRepository.findAll().stream()
                .map(this::convertirADto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OfertaServicioResponseDTO buscarPorId(Integer id) {
        return ofertaRepository.findById(id)
                .map(this::convertirADto)
                .orElseThrow(() -> new RuntimeException("Oferta de servicio no encontrada con ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<OfertaServicioResponseDTO> listarPorTrabajador(Integer idTrabajador) {
        return ofertaRepository.findByIdTrabajadorOrderByFechaPublicacionDesc(idTrabajador).stream()
                .map(this::convertirADto)
                .collect(Collectors.toList());
    }

    // ==========================================
    // 3. ACTUALIZAR
    // ==========================================
    @Transactional
    public OfertaServicioResponseDTO actualizar(Integer idOferta, OfertaServicioUpdateDTO dto, Integer idUsuarioAutenticado) {
        
        OfertaServicio ofertaExistente = ofertaRepository.findById(idOferta)
                .orElseThrow(() -> new RuntimeException("Oferta de servicio no encontrada."));

        if (!ofertaExistente.getIdTrabajador().equals(idUsuarioAutenticado)) {
            throw new RuntimeException("Acceso denegado: No puedes editar una oferta que no te pertenece.");
        }

        if (dto.getTitulo() != null) ofertaExistente.setTitulo(dto.getTitulo());
        if (dto.getDescripcion() != null) ofertaExistente.setDescripcion(dto.getDescripcion());
        if (dto.getPrecio() != null) ofertaExistente.setPrecio(dto.getPrecio());
        if (dto.getDisponible() != null) ofertaExistente.setDisponible(dto.getDisponible());
        
        if (dto.getIdCategoria() != null) {
            CategoriaServicio cat = categoriaRepository.findById(dto.getIdCategoria())
                    .orElseThrow(() -> new RuntimeException("La categoría indicada no existe."));
            ofertaExistente.setCategoriaServicio(cat);
        }

        if (dto.getIdTipoPrecio() != null) {
            TipoPrecio tp = tipoPrecioRepository.findById(dto.getIdTipoPrecio())
                    .orElseThrow(() -> new RuntimeException("El tipo de precio indicado no existe."));
            ofertaExistente.setTipoPrecio(tp);
        }

        OfertaServicio actualizada = ofertaRepository.save(ofertaExistente);
        return convertirADto(actualizada);
    }

    // ==========================================
    // 4. ELIMINAR (Soft Delete)
    // ==========================================
    @Transactional
    public void eliminar(Integer idOferta, Integer idUsuarioAutenticado) {
        OfertaServicio ofertaExistente = ofertaRepository.findById(idOferta)
                .orElseThrow(() -> new RuntimeException("Oferta de servicio no encontrada."));

        if (!ofertaExistente.getIdTrabajador().equals(idUsuarioAutenticado)) {
            throw new RuntimeException("Acceso denegado: No puedes eliminar una oferta que no te pertenece.");
        }

        ofertaExistente.setBorrado(true);
        ofertaRepository.save(ofertaExistente);
    }

    // ==========================================
    // UTILS: Mapeador
    // ==========================================
    private OfertaServicioResponseDTO convertirADto(OfertaServicio oferta) {
        OfertaServicioResponseDTO dto = new OfertaServicioResponseDTO();
        dto.setId(oferta.getId());
        dto.setTitulo(oferta.getTitulo());
        dto.setDescripcion(oferta.getDescripcion());
        dto.setPrecio(oferta.getPrecio());
        dto.setDisponible(oferta.getDisponible());
        dto.setFechaPublicacion(oferta.getFechaPublicacion());
        dto.setIdTrabajador(oferta.getIdTrabajador());
        
        if (oferta.getCategoriaServicio() != null) {
            dto.setCategoria(oferta.getCategoriaServicio().getNombre());
        }
        if (oferta.getTipoPrecio() != null) {
            dto.setTipoPrecio(oferta.getTipoPrecio().getNombre());
        }
        return dto;
    }
}