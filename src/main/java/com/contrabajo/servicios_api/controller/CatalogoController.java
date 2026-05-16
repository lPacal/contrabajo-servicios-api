package com.contrabajo.servicios_api.controller;

import com.contrabajo.servicios_api.dto.CatalogoDTO;
import com.contrabajo.servicios_api.repository.CategoriaServicioRepository;
import com.contrabajo.servicios_api.repository.TipoPrecioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/catalogos")
@RequiredArgsConstructor
public class CatalogoController {

    private final CategoriaServicioRepository categoriaRepository;
    private final TipoPrecioRepository tipoPrecioRepository;

    @GetMapping("/categorias")
    public ResponseEntity<List<CatalogoDTO>> listarCategorias() {
        return ResponseEntity.ok(
                categoriaRepository.findAll().stream()
                        .map(categoria -> new CatalogoDTO(categoria.getId(), categoria.getNombre()))
                        .collect(Collectors.toList())
        );
    }

    @GetMapping("/tipos-precio")
    public ResponseEntity<List<CatalogoDTO>> listarTiposPrecio() {
        return ResponseEntity.ok(
                tipoPrecioRepository.findAll().stream()
                        .map(tipo -> new CatalogoDTO(tipo.getId(), tipo.getNombre()))
                        .collect(Collectors.toList())
        );
    }
}
