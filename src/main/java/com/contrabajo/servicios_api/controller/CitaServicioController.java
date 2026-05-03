package com.contrabajo.servicios_api.controller;

import com.contrabajo.servicios_api.dto.SolicitarCitaDTO;
import com.contrabajo.servicios_api.service.CitaServicioService;
import com.contrabajo.servicios_api.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/citas")
@RequiredArgsConstructor
public class CitaServicioController {

    private final CitaServicioService citaService;
    private final JwtUtil jwtUtil;
    private final HttpServletRequest request;

    // Helper para obtener ID del usuario autenticado
    private Integer getUid() {
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            return jwtUtil.extractId(auth.substring(7));
        }
        throw new RuntimeException("No autorizado.");
    }

    // 1. SOLICITAR
    @PostMapping("/solicitar")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<?> solicitar(@RequestBody SolicitarCitaDTO dto) {
        try {
            String token = request.getHeader("Authorization").substring(7);
            Integer idC = jwtUtil.extractId(token);
            Integer idCoor = jwtUtil.extraerIdCoordenadas(token);
            if (idCoor == null) throw new RuntimeException("Cuenta sin dirección válida.");
            return ResponseEntity.status(201).body(citaService.solicitarServicio(dto, idC, idCoor));
        } catch (Exception e) { return error(e); }
    }

    // --- ACCIONES DEL TRABAJADOR ---

    @PatchMapping("/{id}/aceptar")
    @PreAuthorize("hasRole('TRABAJADOR')")
    public ResponseEntity<?> aceptar(@PathVariable Integer id) {
        return handle(id, "CITA_HANDSHAKE");
    }

    @PatchMapping("/{id}/rechazar")
    @PreAuthorize("hasRole('TRABAJADOR')")
    public ResponseEntity<?> rechazar(@PathVariable Integer id) {
        return handle(id, "CITA_RECHAZADA");
    }

    @PatchMapping("/{id}/comenzar")
    @PreAuthorize("hasRole('TRABAJADOR')")
    public ResponseEntity<?> comenzar(@PathVariable Integer id) {
        return handle(id, "CITA_COMENZANDO");
    }

    @PatchMapping("/{id}/finalizar-trabajo")
    @PreAuthorize("hasRole('TRABAJADOR')")
    public ResponseEntity<?> finalizarTrabajo(@PathVariable Integer id) {
        return handle(id, "CITA_FINALIZANDO");
    }

    // --- ACCIONES DEL CLIENTE ---

    @PatchMapping("/{id}/confirmar-inicio")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<?> confirmarInicio(@PathVariable Integer id) {
        return handle(id, "CITA_EN_PROCESO");
    }

    @PatchMapping("/{id}/confirmar-finalizacion")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<?> confirmarFinalizacion(@PathVariable Integer id) {
        return handle(id, "CITA_FINALIZADO");
    }

    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<?> cancelar(@PathVariable Integer id) {
        return handle(id, "CITA_CANCELADO");
    }

    // Helpers de respuesta
    private ResponseEntity<?> handle(Integer id, String cod) {
        try {
            return ResponseEntity.ok(citaService.cambiarEstadoCita(id, cod, getUid()));
        } catch (Exception e) { return error(e); }
    }

    private ResponseEntity<?> error(Exception e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
}