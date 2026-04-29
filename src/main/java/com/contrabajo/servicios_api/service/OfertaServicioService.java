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
        
        // REGLA 1: Máximo 3 ofertas (No borradas)
        List<OfertaServicio> ofertasVivas = ofertaRepository.findByIdTrabajadorOrderByFechaPublicacionDesc(idUsuarioAutenticado)
                .stream()
                .filter(os -> !os.getBorrado()) // Filtro manual de seguridad
                .collect(Collectors.toList());

        if (ofertasVivas.size() >= 3) {
            throw new RuntimeException("Límite de ofertas alcanzado (Máximo 3). Para crear una nueva, debes eliminar alguna de las existentes.");
        }

        // REGLA 2: Solo 1 puede estar disponible. 
        // Desactivamos todas las anteriores antes de crear la nueva.
        for (OfertaServicio os : ofertasVivas) {
            if (os.getDisponible()) {
                os.setDisponible(false);
                ofertaRepository.save(os);
            }
        }

        OfertaServicio nuevaOferta = new OfertaServicio();
        nuevaOferta.setTitulo(dto.getTitulo());
        nuevaOferta.setDescripcion(dto.getDescripcion());
        nuevaOferta.setPrecio(dto.getPrecio());
        
        // La nueva oferta nace disponible y las otras ya se desactivaron arriba
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
        
        if (ofertaExistente.getBorrado()) {
            throw new RuntimeException("No se puede actualizar una oferta que ha sido eliminada.");
        }

        if (!ofertaExistente.getIdTrabajador().equals(idUsuarioAutenticado)) {
            throw new RuntimeException("Acceso denegado: No puedes editar una oferta que no te pertenece.");
        }

        // REGLA 3: Si se intenta activar esta oferta, hay que desactivar las demás
        if (dto.getDisponible() != null && dto.getDisponible() == true) {
            List<OfertaServicio> vivas = ofertaRepository.findByIdTrabajadorOrderByFechaPublicacionDesc(idUsuarioAutenticado)
                    .stream()
                    .filter(os -> !os.getBorrado()) // Filtro manual vital
                    .collect(Collectors.toList());

            for (OfertaServicio os : vivas) {
                // Desactivamos todas menos la que estamos editando actualmente
                if (!os.getId().equals(idOferta) && os.getDisponible()) {
                    os.setDisponible(false);
                    ofertaRepository.save(os);
                }
            }
            ofertaExistente.setDisponible(true);
        } else if (dto.getDisponible() != null && dto.getDisponible() == false) {
            // Si el usuario simplemente quiso apagar su única oferta activa, lo permitimos
            ofertaExistente.setDisponible(false);
        }

        if (dto.getTitulo() != null) ofertaExistente.setTitulo(dto.getTitulo());
        if (dto.getDescripcion() != null) ofertaExistente.setDescripcion(dto.getDescripcion());
        if (dto.getPrecio() != null) ofertaExistente.setPrecio(dto.getPrecio());
        
        // ... (resto del mapeo de categoría y tipo de precio igual) ...
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
        ofertaExistente.setDisponible(false);
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