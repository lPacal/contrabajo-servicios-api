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
    
    // Spring inyecta esto automáticamente para cada petición
    private final HttpServletRequest request; 

    // ==========================================
    // HELPERS INTERNOS
    // ==========================================
    
    // Helper 1: Extrae solo el Token limpio
    private String obtenerToken() {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new RuntimeException("Acceso denegado: Token inválido o no encontrado.");
    }

    // Helper 2: Extrae el ID (reutilizado en aceptar, cancelar, finalizar)
    private Integer obtenerIdUsuarioAutenticado() {
        return jwtUtil.extractId(obtenerToken());
    }

    // ==========================================
    // ENDPOINTS
    // ==========================================

    // 1. SOLICITAR (Solo Clientes)
    @PostMapping("/solicitar")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<?> solicitarServicio(@RequestBody SolicitarCitaDTO dto) { // Quitamos el HttpServletRequest de los parámetros
        try {
            // 1. Usamos el Helper para sacar el token
            String token = obtenerToken();

            // 2. Extraemos ambos IDs usando tu JwtUtil
            Integer idCliente = jwtUtil.extractId(token);
            Integer idCoordenadas = jwtUtil.extraerIdCoordenadas(token);

            // 3. Validar que el usuario sí tenga coordenadas en su token
            if (idCoordenadas == null) {
                throw new RuntimeException("Error: Tu cuenta no tiene una dirección válida registrada para solicitar servicios.");
            }

            // 4. Pasamos todo a la "máquina" del Service
            CitaServicioResponseDTO nuevaCita = citaService.solicitarServicio(dto, idCliente, idCoordenadas);
            
            return ResponseEntity.status(201).body(nuevaCita);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 2. ACEPTAR (De "Pendiente" a "Aceptado")
    @PatchMapping("/{id}/aceptar")
    @PreAuthorize("hasRole('TRABAJADOR')")
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