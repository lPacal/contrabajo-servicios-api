package com.contrabajo.servicios_api.service;

import com.contrabajo.servicios_api.dto.EstadoDTO;
import com.contrabajo.servicios_api.model.Estado;
import com.contrabajo.servicios_api.repository.EstadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EstadoCatalogoService {

    private final EstadoRepository estadoRepository;

    @Transactional(readOnly = true)
    public List<EstadoDTO> listarEstados() {
        return estadoRepository.findAll(Sort.by(Sort.Direction.ASC, "id"))
                .stream()
                .map(this::toDto)
                .toList();
    }

    private EstadoDTO toDto(Estado estado) {
        return new EstadoDTO(
                estado.getId() != null ? estado.getId().intValue() : null,
                estado.getCodigo(),
                estado.getNombre(),
                estado.getDescripcion()
        );
    }
}
