package com.contrabajo.servicios_api.controller;

import com.contrabajo.servicios_api.dto.CitaServicioResponseDTO;
import com.contrabajo.servicios_api.dto.SolicitarCitaDTO;
import com.contrabajo.servicios_api.service.CitaServicioService;
import com.contrabajo.servicios_api.utils.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/citas")
@RequiredArgsConstructor
public class CitaServicioController {

    private final CitaServicioService citaService;
    private final JwtUtil jwtUtil;
    private final HttpServletRequest request;

    private Integer obtenerIdUsuarioAutenticado() {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return jwtUtil.extractId(authHeader.substring(7));
        }
        throw new RuntimeException("Acceso denegado: Token invalido.");
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 1. SOLICITAR — cliente crea la cita en PENDIENTE
    // ──────────────────────────────────────────────────────────────────────────
    @PostMapping("/solicitar")
    @PreAuthorize("hasAnyRole('CLIENTE','PREMIUM')")
    public ResponseEntity<?> solicitar(@RequestBody SolicitarCitaDTO dto) {
        try {
            return ResponseEntity.status(201)
                    .body(citaService.solicitarServicio(dto, obtenerIdUsuarioAutenticado()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 2. ACEPTAR — PENDIENTE → HANDSHAKE (trabajador)
    // ──────────────────────────────────────────────────────────────────────────
    @PatchMapping("/{id}/aceptar")
    @PreAuthorize("hasAnyRole('TRABAJADOR','PREMIUM')")
    public ResponseEntity<?> aceptar(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(citaService.aceptarCita(id, obtenerIdUsuarioAutenticado()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 3. RECHAZAR — PENDIENTE → RECHAZADA (trabajador)
    // ──────────────────────────────────────────────────────────────────────────
    @PatchMapping("/{id}/rechazar")
    @PreAuthorize("hasAnyRole('TRABAJADOR','PREMIUM')")
    public ResponseEntity<?> rechazar(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(citaService.rechazarCita(id, obtenerIdUsuarioAutenticado()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 4. REENVIAR PROPUESTA — RECHAZADA → PENDIENTE (cliente)
    // ──────────────────────────────────────────────────────────────────────────
    @PatchMapping("/{id}/reenviar")
    @PreAuthorize("hasAnyRole('CLIENTE','PREMIUM')")
    public ResponseEntity<?> reenviar(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(citaService.reenviarPropuesta(id, obtenerIdUsuarioAutenticado()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 5. COMENZAR — HANDSHAKE → COMENZANDO (trabajador solicita inicio)
    // ──────────────────────────────────────────────────────────────────────────
    @PatchMapping("/{id}/comenzar")
    @PreAuthorize("hasAnyRole('TRABAJADOR','PREMIUM')")
    public ResponseEntity<?> comenzar(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(citaService.comenzarTrabajo(id, obtenerIdUsuarioAutenticado()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 6. CONFIRMAR INICIO — COMENZANDO → EN_PROCESO (cliente confirma)
    // ──────────────────────────────────────────────────────────────────────────
    @PatchMapping("/{id}/confirmar-inicio")
    @PreAuthorize("hasAnyRole('CLIENTE','PREMIUM')")
    public ResponseEntity<?> confirmarInicio(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(citaService.confirmarInicio(id, obtenerIdUsuarioAutenticado()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 7. FINALIZAR — EN_PROCESO → FINALIZANDO (trabajador solicita cierre)
    // ──────────────────────────────────────────────────────────────────────────
    @PatchMapping("/{id}/finalizar")
    @PreAuthorize("hasAnyRole('TRABAJADOR','PREMIUM')")
    public ResponseEntity<?> finalizar(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(citaService.finalizarTrabajo(id, obtenerIdUsuarioAutenticado()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 8. CONFIRMAR FINALIZACION — FINALIZANDO → FINALIZADO (cliente confirma)
    // ──────────────────────────────────────────────────────────────────────────
    @PatchMapping("/{id}/confirmar-finalizacion")
    @PreAuthorize("hasAnyRole('CLIENTE','PREMIUM')")
    public ResponseEntity<?> confirmarFinalizacion(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(citaService.confirmarFinalizacion(id, obtenerIdUsuarioAutenticado()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 9. CANCELAR — cualquier estado activo → CANCELADO (cualquiera)
    // ──────────────────────────────────────────────────────────────────────────
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelar(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(citaService.cancelarCita(id, obtenerIdUsuarioAutenticado()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 10. LISTAR MIS CITAS
    // ──────────────────────────────────────────────────────────────────────────
    @GetMapping("/mis-citas")
    public ResponseEntity<List<CitaServicioResponseDTO>> misCitas() {
        return ResponseEntity.ok(citaService.listarMisCitas(obtenerIdUsuarioAutenticado()));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 11. DETALLE DE UNA CITA
    // ──────────────────────────────────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<?> detalle(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(citaService.obtenerCita(id, obtenerIdUsuarioAutenticado()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
