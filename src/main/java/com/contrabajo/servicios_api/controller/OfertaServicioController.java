package com.contrabajo.servicios_api.controller;

import com.contrabajo.servicios_api.dto.OfertaServicioCreateDTO;
import com.contrabajo.servicios_api.dto.OfertaServicioResponseDTO;
import com.contrabajo.servicios_api.dto.OfertaServicioUpdateDTO;
import com.contrabajo.servicios_api.service.OfertaServicioService;
import com.contrabajo.servicios_api.utils.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ofertas")
@RequiredArgsConstructor
@EnableMethodSecurity
public class OfertaServicioController {

    private final OfertaServicioService ofertaService;
    private final JwtUtil jwtUtil; 
    private final HttpServletRequest request;

    // Método para extraer el ID del Token
    private Integer obtenerIdUsuarioAutenticado() {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return jwtUtil.extractId(token); 
        }
        throw new RuntimeException("Acceso denegado: No se encontró un token JWT válido.");
    }

    // Crear oferta (Solo Trabajadores)
    @PostMapping
    @PreAuthorize("hasRole('TRABAJADOR')")
    public ResponseEntity<?> crearOferta(@RequestBody OfertaServicioCreateDTO dto) {
        try {
            Integer idUsuario = obtenerIdUsuarioAutenticado();
            OfertaServicioResponseDTO nuevaOferta = ofertaService.crear(dto, idUsuario);
            return ResponseEntity.status(201).body(nuevaOferta);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Listar todas
    @GetMapping
    public ResponseEntity<List<OfertaServicioResponseDTO>> listarTodas() {
        return ResponseEntity.ok(ofertaService.listarTodas());
    }

    // Buscar por ID
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(ofertaService.buscarPorId(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    // Listar por trabajador
    @GetMapping("/trabajador/{idTrabajador}")
    public ResponseEntity<List<OfertaServicioResponseDTO>> listarPorTrabajador(@PathVariable Integer idTrabajador) {
        return ResponseEntity.ok(ofertaService.listarPorTrabajador(idTrabajador));
    }

    // Actualizar oferta
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('TRABAJADOR')")
    public ResponseEntity<?> actualizarOferta(
            @PathVariable Integer id, 
            @RequestBody OfertaServicioUpdateDTO dto) {
        try {
            Integer idUsuario = obtenerIdUsuarioAutenticado();
            OfertaServicioResponseDTO ofertaActualizada = ofertaService.actualizar(id, dto, idUsuario);
            return ResponseEntity.ok(ofertaActualizada);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Eliminar oferta
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TRABAJADOR')")
    public ResponseEntity<?> eliminarOferta(@PathVariable Integer id) {
        try {
            Integer idUsuario = obtenerIdUsuarioAutenticado();
            ofertaService.eliminar(id, idUsuario);
            return ResponseEntity.ok(Map.of("mensaje", "Oferta eliminada correctamente."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}