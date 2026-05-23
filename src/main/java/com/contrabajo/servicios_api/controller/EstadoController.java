package com.contrabajo.servicios_api.controller;

import com.contrabajo.servicios_api.dto.EstadoDTO;
import com.contrabajo.servicios_api.service.EstadoCatalogoService;
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

@RestController
@Tag(name = "2. Estados", description = "Endpoints de consulta para estados del flujo de citas")
@RequestMapping("/api/estados")
@RequiredArgsConstructor
public class EstadoController {

    private final EstadoCatalogoService estadoCatalogoService;

    @GetMapping
    @Operation(summary = "Listar estados de cita", description = "Devuelve el catalogo de estados usados por el flujo de citas de servicio.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de estados.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = EstadoDTO.class)))
    })
    public ResponseEntity<List<EstadoDTO>> listarEstados() {
        return ResponseEntity.ok(estadoCatalogoService.listarEstados());
    }
}
