package com.contrabajo.servicios_api.controller;

import com.contrabajo.servicios_api.dto.ValoracionRequestDTO;
import com.contrabajo.servicios_api.service.ValoracionService;
import com.contrabajo.servicios_api.utils.JwtUtil; // Asegúrate de importar tu JwtUtil
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/valoraciones")
@RequiredArgsConstructor
public class ValoracionController {

    private final ValoracionService valoracionService;
    private final JwtUtil jwtUtil; // Inyectamos la herramienta para leer tokens

    @PostMapping
    public ResponseEntity<?> crearValoracion(@RequestBody ValoracionRequestDTO dto, HttpServletRequest request) {
        try {
            // 1. Capturamos el token de la cabecera
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new RuntimeException("Token de autorización no encontrado.");
            }
            String token = authHeader.substring(7); // Quitamos la palabra "Bearer "

            // 2. Extraemos el ID del cliente directamente de la "maleta" del token
            Integer idClienteAutenticado = jwtUtil.extractClaim(token, claims -> claims.get("id", Integer.class));
            
            // 3. Pasamos el ID al Service
            valoracionService.crearValoracion(dto, idClienteAutenticado);
            
            return ResponseEntity.ok(Map.of("mensaje", "La valoración ha sido registrada con éxito."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ... (Tus otros endpoints GET se mantienen igual) ...
}