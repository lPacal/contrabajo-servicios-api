package com.contrabajo.servicios_api.service;

import com.contrabajo.servicios_api.dto.FotoResponseDTO;
import com.contrabajo.servicios_api.model.Foto;
import com.contrabajo.servicios_api.model.OfertaServicio;
import com.contrabajo.servicios_api.repository.FotoRepository;
import com.contrabajo.servicios_api.repository.OfertaServicioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FotoService {

    private final FotoRepository fotoRepository;
    private final OfertaServicioRepository ofertaRepository;

    public FotoService(FotoRepository fotoRepository,
                       OfertaServicioRepository ofertaRepository) {
        this.fotoRepository = fotoRepository;
        this.ofertaRepository = ofertaRepository;
    }

    // ── Registrar o REEMPLAZAR enlace público de foto en oferta ───────────────
    @Transactional
    public FotoResponseDTO guardarUrl(String url, Integer idOferta, Integer idUsuario) {

        // 1. Validar existencia de la oferta de servicio
        OfertaServicio oferta = ofertaRepository.findById(idOferta)
                .orElseThrow(() -> new RuntimeException("Oferta no encontrada."));
        
        // 2. Control estricto de propiedad (Evita vulnerabilidad IDOR)
        if (!oferta.getIdTrabajador().equals(idUsuario)) {
            throw new RuntimeException("No tienes permiso para subir fotos a esta oferta.");
        }

        // 3. CAMBIO AQUÍ: Buscar si la oferta ya tiene fotos asociadas en la BD
        List<Foto> existentes = fotoRepository.findByOfertaServicioId(idOferta);
        Foto foto;

        if (!existentes.isEmpty()) {
            // CASO UPDATE: Si ya tenía foto, reutilizamos la primera fila existente
            foto = existentes.get(0);
            
            // Resguardo técnico: Si por residuos de desarrollo previos hubiese más de una, 
            // purgamos las excedentes de la base de datos para sanear la integridad.
            for (int i = 1; i < existentes.size(); i++) {
                fotoRepository.delete(existentes.get(i));
            }
        } else {
            // CASO INSERT: Si es la primera vez que se le asigna foto a la oferta
            foto = new Foto();
            foto.setOfertaServicio(oferta);
            foto.setIdUsuario(idUsuario);
        }

        // 4. Seteamos el nuevo enlace de Cloudinary (sea registro nuevo o actualización)
        foto.setEnlace(url);

        // 5. Persistir en SQL Server y transformar al DTO estructurado
        return toDTO(fotoRepository.save(foto));
    }

    // ── Listar todas las fotos vinculadas a una oferta ────────────────────────
    public List<FotoResponseDTO> listarPorOferta(Integer idOferta) {
        return fotoRepository.findByOfertaServicioId(idOferta)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // ── Eliminar registro de foto multimedia ──────────────────────────────────
    @Transactional
    public void eliminar(Integer idFoto, Integer idUsuario) {
        Foto foto = fotoRepository.findById(idFoto)
                .orElseThrow(() -> new RuntimeException("Foto no encontrada."));

        // Impedir que un tercero elimine fotos adjuntas de otra cuenta
        if (!foto.getIdUsuario().equals(idUsuario)) {
            throw new RuntimeException("No tienes permiso para eliminar esta foto.");
        }

        // Eliminación relacional directa en cascada
        fotoRepository.delete(foto);
    }

    // ── Helper de mapeo adaptado al nuevo contrato del constructor ───────────
    private FotoResponseDTO toDTO(Foto f) {
        return new FotoResponseDTO(
                f.getId(),
                f.getEnlace(),
                f.getFechaSubida(),
                f.getOfertaServicio().getId(),
                f.getIdUsuario()
        );
    }
}