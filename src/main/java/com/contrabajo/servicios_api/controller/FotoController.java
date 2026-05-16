package com.contrabajo.servicios_api.controller;

import com.contrabajo.servicios_api.dto.FotoResponseDTO;
import com.contrabajo.servicios_api.service.FotoService;
import com.contrabajo.servicios_api.utils.JwtUtil;
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
    public ResponseEntity<List<FotoResponseDTO>> listarPorOferta(@PathVariable Integer idOferta) {
        return ResponseEntity.ok(fotoService.listarPorOferta(idOferta));
    }

    // ── DELETE /api/fotos/{idFoto}  ──────────────────────────────────────────
    // Elimina una foto (disco + BD). Solo el propietario.
    @DeleteMapping("/{idFoto}")
    @PreAuthorize("hasAnyRole('TRABAJADOR', 'PREMIUM')")
    public ResponseEntity<?> eliminar(@PathVariable Integer idFoto) {
        try {
            fotoService.eliminar(idFoto, idUsuarioActual());
            return ResponseEntity.ok(Map.of("mensaje", "Foto eliminada correctamente."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
