package com.contrabajo.servicios_api.controller;

import com.contrabajo.servicios_api.dto.FotoRequestDTO;
import com.contrabajo.servicios_api.dto.FotoResponseDTO;
import com.contrabajo.servicios_api.service.FotoService;
import com.contrabajo.servicios_api.utils.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@Tag(name = "5. Fotos de ofertas", description = "Endpoints para registrar, listar y eliminar fotos asociadas a ofertas")
@RequestMapping("/api/fotos")
@RequiredArgsConstructor
public class FotoController {

    private final FotoService fotoService;
    private final JwtUtil jwtUtil;
    private final HttpServletRequest request;

    private Integer idUsuarioActual() {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return jwtUtil.extractId(header.substring(7));
        }
        throw new RuntimeException("Token no encontrado.");
    }

    // ── POST /api/fotos/{idOferta} ──────────────────────────────────────────
    // Registra una URL de Cloudinary para la oferta indicada.
    @PostMapping("/{idOferta}")
    @PreAuthorize("hasAnyRole('TRABAJADOR', 'PREMIUM')")
    @Operation(
            summary = "Vincular foto a oferta",
            description = "**Requiere rol TRABAJADOR o PREMIUM (BearerAuth)**<br><br>" +
                          "Asocia el enlace público de Cloudinary a la oferta indicada. El usuario autenticado debe ser el dueño de la oferta."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Enlace registrado correctamente.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = FotoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "URL inválida, oferta inexistente o usuario sin permisos.",
                    content = @Content(mediaType = "application/json", 
                        examples = {
                            @ExampleObject(name = "1. URL Obligatoria", value = "{\"error\": \"La URL de la foto es obligatoria.\"}"),
                            @ExampleObject(name = "2. Restricción Propietario", value = "{\"error\": \"No tienes permiso para subir fotos a esta oferta.\"}")
                        })),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente, inválido o expirado.", content = @Content),
            @ApiResponse(responseCode = "403", description = "Rol no autorizado para esta operación.", content = @Content)
    })
    public ResponseEntity<?> registrarFoto(
            @PathVariable Integer idOferta,
            @RequestBody FotoRequestDTO dto) {
        try {
            if (dto.getUrl() == null || dto.getUrl().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "La URL de la foto es obligatoria."));
            }
            FotoResponseDTO responseDto = fotoService.guardarUrl(dto.getUrl(), idOferta, idUsuarioActual());
            return ResponseEntity.status(201).body(responseDto);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── GET /api/fotos/oferta/{idOferta} ────────────────────────────────────
    @GetMapping("/oferta/{idOferta}")
    @Operation(summary = "Listar fotos por oferta", description = "Devuelve todas las fotos asociadas a una oferta de servicio.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de fotos obtenida.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = FotoResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente o inválido.", content = @Content)
    })
    public ResponseEntity<List<FotoResponseDTO>> listarPorOferta(@PathVariable Integer idOferta) {
        return ResponseEntity.ok(fotoService.listarPorOferta(idOferta));
    }

    // ── DELETE /api/fotos/{idFoto} ──────────────────────────────────────────
    @DeleteMapping("/{idFoto}")
    @PreAuthorize("hasAnyRole('TRABAJADOR', 'PREMIUM')")
    @Operation(
            summary = "Eliminar foto de oferta",
            description = "**Requiere rol TRABAJADOR o PREMIUM (BearerAuth)**<br><br>" +
                          "Elimina permanentemente el registro de la foto en la base de datos SQL Server."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Registro eliminado correctamente.",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"mensaje\": \"Foto eliminada correctamente.\"}"))),
            @ApiResponse(responseCode = "400", description = "Foto inexistente o usuario sin privilegios sobre la oferta.",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"error\": \"No tienes permiso para eliminar esta foto.\"}"))),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente o inválido.", content = @Content)
    })
    public ResponseEntity<?> eliminar(@PathVariable Integer idFoto) {
        try {
            fotoService.eliminar(idFoto, idUsuarioActual());
            return ResponseEntity.ok(Map.of("mensaje", "Foto deleted exitosamente."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}