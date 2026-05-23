package com.contrabajo.servicios_api.controller;

import com.contrabajo.servicios_api.dto.CatalogoDTO;
import com.contrabajo.servicios_api.repository.CategoriaServicioRepository;
import com.contrabajo.servicios_api.repository.TipoPrecioRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@Tag(name = "1. Catalogos", description = "Endpoints publicos para catalogos usados por ofertas de servicio")
@RequestMapping("/api/catalogos")
@RequiredArgsConstructor
public class CatalogoController {

    private final CategoriaServicioRepository categoriaRepository;
    private final TipoPrecioRepository tipoPrecioRepository;

    @GetMapping("/categorias")
    @Operation(summary = "Listar categorias de servicio", description = "Devuelve las categorias disponibles para clasificar ofertas de servicio. No requiere autenticacion.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de categorias.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CatalogoDTO.class)))
    })
    public ResponseEntity<List<CatalogoDTO>> listarCategorias() {
        return ResponseEntity.ok(
                categoriaRepository.findAll().stream()
                        .map(categoria -> new CatalogoDTO(categoria.getId(), categoria.getNombre()))
                        .collect(Collectors.toList())
        );
    }

    @GetMapping("/tipos-precio")
    @Operation(summary = "Listar tipos de precio", description = "Devuelve los tipos de precio disponibles para publicar ofertas. No requiere autenticacion.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de tipos de precio.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CatalogoDTO.class)))
    })
    public ResponseEntity<List<CatalogoDTO>> listarTiposPrecio() {
        return ResponseEntity.ok(
                tipoPrecioRepository.findAll().stream()
                        .map(tipo -> new CatalogoDTO(tipo.getId(), tipo.getNombre()))
                        .collect(Collectors.toList())
        );
    }
}
