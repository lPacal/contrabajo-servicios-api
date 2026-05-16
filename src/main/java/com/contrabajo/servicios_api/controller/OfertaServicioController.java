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

    private String obtenerRolUsuarioAutenticado() {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return jwtUtil.extractRol(token);
        }
        throw new RuntimeException("Acceso denegado: No se encontró un token JWT válido.");
    }

    private String obtenerAuthorizationHeader() {
        return request.getHeader("Authorization");
    }

    // Crear oferta (Solo Trabajadores)
    @PostMapping
    @PreAuthorize("hasAnyRole('TRABAJADOR', 'PREMIUM')")
    public ResponseEntity<?> crearOferta(@RequestBody OfertaServicioCreateDTO dto) {
        try {
            Integer idUsuario = obtenerIdUsuarioAutenticado();
            OfertaServicioResponseDTO nuevaOferta = ofertaService.crear(dto, idUsuario, obtenerAuthorizationHeader());
            return ResponseEntity.status(201).body(nuevaOferta);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Listar todas
    @GetMapping
    public ResponseEntity<List<OfertaServicioResponseDTO>> listarTodas() {
        return ResponseEntity.ok(ofertaService.listarTodas(obtenerAuthorizationHeader()));
    }

    // Buscar por ID
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(ofertaService.buscarPorId(id, obtenerAuthorizationHeader()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}/disponibilidad")
    @PreAuthorize("hasAnyRole('TRABAJADOR', 'PREMIUM')")
    public ResponseEntity<Boolean> obtenerDisponibilidad(@PathVariable Integer id) {
        try {
            Integer idUsuario = obtenerIdUsuarioAutenticado();
            return ResponseEntity.ok(ofertaService.obtenerDisponibilidad(id, idUsuario));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(false);
        }
    }

    // Listar por trabajador
    @GetMapping("/trabajador/{idTrabajador}")
    public ResponseEntity<List<OfertaServicioResponseDTO>> listarPorTrabajador(@PathVariable Integer idTrabajador) {
        return ResponseEntity.ok(ofertaService.listarPorTrabajador(idTrabajador, obtenerAuthorizationHeader()));
    }

    // Actualizar oferta
    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('TRABAJADOR', 'PREMIUM')")
    public ResponseEntity<?> actualizarOferta(
            @PathVariable Integer id, 
            @RequestBody OfertaServicioUpdateDTO dto) {
        try {
            Integer idUsuario = obtenerIdUsuarioAutenticado();
            OfertaServicioResponseDTO ofertaActualizada = ofertaService.actualizar(id, dto, idUsuario, obtenerAuthorizationHeader());
            return ResponseEntity.ok(ofertaActualizada);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TRABAJADOR', 'PREMIUM')")
    public ResponseEntity<?> actualizarOfertaPut(
            @PathVariable Integer id,
            @RequestBody OfertaServicioUpdateDTO dto) {
        return actualizarOferta(id, dto);
    }

    @PatchMapping("/{id}/disponibilidad/activar")
    @PreAuthorize("hasAnyRole('TRABAJADOR', 'PREMIUM')")
    public ResponseEntity<?> activarDisponibilidad(@PathVariable Integer id) {
        try {
            Integer idUsuario = obtenerIdUsuarioAutenticado();
            OfertaServicioResponseDTO ofertaActualizada = ofertaService.activarDisponibilidad(id, idUsuario, obtenerAuthorizationHeader());
            return ResponseEntity.ok(ofertaActualizada);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/disponibilidad/desactivar")
    @PreAuthorize("hasAnyRole('TRABAJADOR', 'PREMIUM', 'MODERADOR', 'ADMINISTRADOR')")
    public ResponseEntity<?> desactivarDisponibilidad(@PathVariable Integer id) {
        try {
            Integer idUsuario = obtenerIdUsuarioAutenticado();
            String rol = obtenerRolUsuarioAutenticado();
            OfertaServicioResponseDTO ofertaActualizada = ofertaService.desactivarDisponibilidad(id, idUsuario, rol, obtenerAuthorizationHeader());
            return ResponseEntity.ok(ofertaActualizada);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Eliminar oferta
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('TRABAJADOR', 'PREMIUM', 'MODERADOR', 'ADMINISTRADOR')")
    public ResponseEntity<?> eliminarOferta(@PathVariable Integer id) {
        try {
            Integer idUsuario = obtenerIdUsuarioAutenticado();
            String rol = obtenerRolUsuarioAutenticado();
            ofertaService.eliminar(id, idUsuario, rol);
            return ResponseEntity.ok(Map.of("mensaje", "Oferta eliminada correctamente."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
