package com.contrabajo.servicios_api.service;

import com.contrabajo.servicios_api.dto.ValoracionRequestDTO;
import com.contrabajo.servicios_api.dto.ValoracionResponseDTO;
import com.contrabajo.servicios_api.model.CitaServicio;
import com.contrabajo.servicios_api.model.Valoracion;
import com.contrabajo.servicios_api.repository.CitaServicioRepository;
import com.contrabajo.servicios_api.repository.ValoracionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ValoracionService {

    private final ValoracionRepository valoracionRepository;
    private final CitaServicioRepository citaRepository;

    // ==========================================
    // 1. CREAR VALORACIÓN
    // ==========================================
    @Transactional
    public void crearValoracion(ValoracionRequestDTO dto, Integer idClienteAutenticado) {
        
        if (dto.getVoto() == null || dto.getVoto() < 1 || dto.getVoto() > 5) {
            throw new RuntimeException("La valoración debe estar entre 1 y 5 estrellas.");
        }

        CitaServicio cita = citaRepository.findById(dto.getIdCita())
                .orElseThrow(() -> new RuntimeException("Cita no encontrada."));

        // ¡AQUÍ ESTÁ LA MAGIA CORREGIDA! 
        // Comparamos el ID de la cita con el ID que venía adentro del Token
        if (!cita.getIdCliente().equals(idClienteAutenticado)) {
            throw new RuntimeException("Acceso denegado: No tienes permisos para valorar esta cita.");
        }

        // Ajusta "FINALIZADO" al nombre exacto o ID de tu EstadoCita
        if (!"FINALIZADO".equalsIgnoreCase(cita.getEstado().getNombre())) {
            throw new RuntimeException("Solo puedes valorar citas que ya hayan sido finalizadas.");
        }

        if (valoracionRepository.existsByCita(cita)) {
            throw new RuntimeException("Ya has enviado una valoración para este servicio.");
        }

        Valoracion valoracion = new Valoracion();
        valoracion.setCita(cita);
        valoracion.setVoto(dto.getVoto());
        valoracion.setComentario(dto.getComentario());
        
        valoracion.setIdCliente(cita.getIdCliente());
        valoracion.setIdTrabajador(cita.getIdTrabajador());

        valoracionRepository.save(valoracion);
    }

    // ==========================================
    // 2. LEER VALORACIONES (Usando tu Repositorio)
    // ==========================================
    @Transactional(readOnly = true)
    public List<ValoracionResponseDTO> obtenerPorTrabajador(Integer idTrabajador) {
        return valoracionRepository.findByIdTrabajadorOrderByFechaVotoDesc(idTrabajador)
                .stream()
                .map(this::convertirADto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ValoracionResponseDTO> obtenerPorCliente(Integer idCliente) {
        return valoracionRepository.findByIdClienteOrderByFechaVotoDesc(idCliente)
                .stream()
                .map(this::convertirADto)
                .collect(Collectors.toList());
    }

    // Traductor Entidad -> DTO
    private ValoracionResponseDTO convertirADto(Valoracion valoracion) {
        ValoracionResponseDTO dto = new ValoracionResponseDTO();
        dto.setId(valoracion.getId());
        dto.setIdCita(valoracion.getCita().getId()); // Asumiendo que getId() en CitaServicio devuelve Integer
        dto.setIdCliente(valoracion.getIdCliente());
        dto.setIdTrabajador(valoracion.getIdTrabajador());
        dto.setVoto(valoracion.getVoto());
        dto.setComentario(valoracion.getComentario());
        dto.setFechaVoto(valoracion.getFechaVoto());
        return dto;
    }
}