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

import java.util.Map;

@RestController
@RequestMapping("/api/citas")
@RequiredArgsConstructor
public class CitaServicioController {

    private final CitaServicioService citaService;
    private final JwtUtil jwtUtil;
    private final HttpServletRequest request;

    // Método interno para extraer el ID del Token (igual que en Ofertas)
    private Integer obtenerIdUsuarioAutenticado() {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return jwtUtil.extractId(token);
        }
        throw new RuntimeException("Acceso denegado: Token inválido.");
    }

    // 1. SOLICITAR (Solo Clientes)
    @PostMapping("/solicitar")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<?> solicitarServicio(@RequestBody SolicitarCitaDTO dto) {
        try {
            Integer idCliente = obtenerIdUsuarioAutenticado();
            CitaServicioResponseDTO nuevaCita = citaService.solicitarServicio(dto, idCliente);
            return ResponseEntity.status(201).body(nuevaCita);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 2. ACEPTAR (De "Pendiente" a "Aceptado")
    @PatchMapping("/{id}/aceptar")
    @PreAuthorize("hasRole('TRABAJADOR')") // Solo el trabajador puede decir "Sí, voy"
    public ResponseEntity<?> aceptarCita(@PathVariable Integer id) {
        try {
            Integer idUsuario = obtenerIdUsuarioAutenticado();
            CitaServicioResponseDTO cita = citaService.cambiarEstadoCita(id, "ACEP", idUsuario);
            return ResponseEntity.ok(cita);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 3. CANCELAR (Cualquiera de los dos puede arrepentirse)
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelarCita(@PathVariable Integer id) {
        try {
            Integer idUsuario = obtenerIdUsuarioAutenticado();
            CitaServicioResponseDTO cita = citaService.cambiarEstadoCita(id, "CANC", idUsuario);
            return ResponseEntity.ok(cita);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 4. FINALIZAR (El trabajo se completó)
    @PatchMapping("/{id}/finalizar")
    @PreAuthorize("hasRole('TRABAJADOR')")
    public ResponseEntity<?> finalizarCita(@PathVariable Integer id) {
        try {
            Integer idUsuario = obtenerIdUsuarioAutenticado();
            CitaServicioResponseDTO cita = citaService.cambiarEstadoCita(id, "FINA", idUsuario);
            return ResponseEntity.ok(cita);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}