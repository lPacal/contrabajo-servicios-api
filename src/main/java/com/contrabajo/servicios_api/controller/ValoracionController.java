package com.contrabajo.servicios_api.controller;

import com.contrabajo.servicios_api.dto.ValoracionRequestDTO;
import com.contrabajo.servicios_api.service.ValoracionService;
import com.contrabajo.servicios_api.utils.JwtUtil; // Asegúrate de importar tu JwtUtil
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/valoraciones")
@RequiredArgsConstructor
public class ValoracionController {

    private final ValoracionService valoracionService;
    private final JwtUtil jwtUtil;
    private final HttpServletRequest request; // Ya inyectado por Lombok

    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')") // <--- BLOQUEO DE SEGURIDAD NIVEL MÉTODO
    public ResponseEntity<?> crearValoracion(@RequestBody ValoracionRequestDTO dto) {
        try {
            // 1. Extraemos el token usando el helper o directamente
            String authHeader = request.getHeader("Authorization");
            String token = authHeader.substring(7);

            // 2. Extraemos el ID del cliente
            Integer idClienteAutenticado = jwtUtil.extractId(token);
            
            // 3. Pasamos el ID al Service
            valoracionService.crearValoracion(dto, idClienteAutenticado);
            
            return ResponseEntity.ok(Map.of("mensaje", "La valoración ha sido registrada con éxito."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}