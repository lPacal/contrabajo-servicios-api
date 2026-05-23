package com.contrabajo.servicios_api.controller;

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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@Tag(name = "5. Fotos de ofertas", description = "Endpoints para subir, listar y eliminar fotos asociadas a ofertas")
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

    // ── POST /api/fotos/{idOferta}  ──────────────────────────────────────────
    // Sube una imagen a la oferta indicada. Solo el propietario (TRABAJADOR/PREMIUM).
    @PostMapping(value = "/{idOferta}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('TRABAJADOR', 'PREMIUM')")
    @Operation(
            summary = "Subir foto de oferta",
            description = "**Requiere rol TRABAJADOR o PREMIUM (BearerAuth)**<br><br>" +
                    "Sube una imagen a la oferta indicada. El usuario autenticado debe ser propietario de la oferta."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Foto subida correctamente.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = FotoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Archivo invalido, oferta inexistente o usuario sin permisos.",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"error\": \"Token no encontrado.\"}"))),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente, invalido o expirado.", content = @Content),
            @ApiResponse(responseCode = "403", description = "Rol no autorizado para subir fotos.", content = @Content)
    })
    public ResponseEntity<?> subirFoto(
            @PathVariable Integer idOferta,
            @RequestPart("imagen") MultipartFile imagen) {
        try {
            FotoResponseDTO dto = fotoService.subir(imagen, idOferta, idUsuarioActual());
            return ResponseEntity.status(201).body(dto);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── GET /api/fotos/oferta/{idOferta}  ────────────────────────────────────
    // Lista todas las fotos de una oferta. Acceso autenticado.
    @GetMapping("/oferta/{idOferta}")
    @Operation(summary = "Listar fotos por oferta", description = "Devuelve todas las fotos asociadas a una oferta de servicio.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de fotos.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = FotoResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente, invalido o expirado.", content = @Content),
            @ApiResponse(responseCode = "403", description = "Rol no autorizado por la configuracion de seguridad.", content = @Content)
    })
    public ResponseEntity<List<FotoResponseDTO>> listarPorOferta(@PathVariable Integer idOferta) {
        return ResponseEntity.ok(fotoService.listarPorOferta(idOferta));
    }

    // ── DELETE /api/fotos/{idFoto}  ──────────────────────────────────────────
    // Elimina una foto (disco + BD). Solo el propietario.
    @DeleteMapping("/{idFoto}")
    @PreAuthorize("hasAnyRole('TRABAJADOR', 'PREMIUM')")
    @Operation(
            summary = "Eliminar foto de oferta",
            description = "**Requiere rol TRABAJADOR o PREMIUM (BearerAuth)**<br><br>" +
                    "Elimina una foto de la oferta. Solo puede hacerlo el propietario de la oferta."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Foto eliminada correctamente.",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"mensaje\": \"Foto eliminada correctamente.\"}"))),
            @ApiResponse(responseCode = "400", description = "Foto inexistente, token ausente o usuario sin permisos.",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"error\": \"Token no encontrado.\"}"))),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente, invalido o expirado.", content = @Content),
            @ApiResponse(responseCode = "403", description = "Rol no autorizado para eliminar fotos.", content = @Content)
    })
    public ResponseEntity<?> eliminar(@PathVariable Integer idFoto) {
        try {
            fotoService.eliminar(idFoto, idUsuarioActual());
            return ResponseEntity.ok(Map.of("mensaje", "Foto eliminada correctamente."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
