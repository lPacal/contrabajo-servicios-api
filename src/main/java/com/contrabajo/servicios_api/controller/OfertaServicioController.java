package com.contrabajo.servicios_api.controller;

import com.contrabajo.servicios_api.model.OfertaServicio;
import com.contrabajo.servicios_api.service.OfertaServicioService;
import com.contrabajo.servicios_api.utils.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ofertas")
@RequiredArgsConstructor
public class OfertaServicioController {

    private final OfertaServicioService ofertaService;
    private final JwtUtil jwtUtil; // Tu clase utilitaria de JWT
    private final HttpServletRequest request;

    // =======================================================
    // MÉTODO AUXILIAR PARA EXTRAER DATOS DEL JWT (Simulado)
    // =======================================================
    // En tu proyecto real, deberías inyectar tu JwtUtil 
    // y extraer estos valores del token que viene en el Header
    private String extraerToken() {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7); // Corta los 7 caracteres de "Bearer "
        }
        throw new RuntimeException("Acceso denegado: No se encontró un token JWT en la petición.");
    }

    // 2. Extraer el ID
    private Integer obtenerIdUsuarioAutenticado() {
        String token = extraerToken();
        // Dependiendo de cómo programaste tu JwtUtil, usa el método correspondiente.
        // Ejemplo si tienes un método específico:
        return jwtUtil.extractId(token); 
    }

    // 3. Extraer el Rol
    private String obtenerRolUsuarioAutenticado() {
        String token = extraerToken();
        // Al igual que el ID, usa el método que lee el Claim del rol en tu JwtUtil
        return jwtUtil.extractRol(token); 
    }

    // =======================================================
    // ENDPOINTS CRUD
    // =======================================================

    @PostMapping
    public ResponseEntity<OfertaServicio> crearOferta(@RequestBody OfertaServicio oferta) {
        Integer idUsuario = obtenerIdUsuarioAutenticado();
        String rol = obtenerRolUsuarioAutenticado();
        
        OfertaServicio nuevaOferta = ofertaService.crear(oferta, idUsuario, rol);
        return ResponseEntity.ok(nuevaOferta);
    }

    @GetMapping
    public ResponseEntity<List<OfertaServicio>> listarTodas() {
        // Gracias a @SQLRestriction, esto ya NO traerá las ofertas borradas
        return ResponseEntity.ok(ofertaService.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OfertaServicio> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(ofertaService.buscarPorId(id));
    }

    @GetMapping("/trabajador/{idTrabajador}")
    public ResponseEntity<List<OfertaServicio>> listarPorTrabajador(@PathVariable Integer idTrabajador) {
        return ResponseEntity.ok(ofertaService.listarPorTrabajador(idTrabajador));
    }

    // Usamos PATCH porque es una actualización parcial
    @PatchMapping("/{id}")
    public ResponseEntity<OfertaServicio> actualizarOferta(
            @PathVariable Integer id, 
            @RequestBody OfertaServicio datosNuevos) {
        
        Integer idUsuario = obtenerIdUsuarioAutenticado();
        OfertaServicio ofertaActualizada = ofertaService.actualizar(id, datosNuevos, idUsuario);
        return ResponseEntity.ok(ofertaActualizada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarOferta(@PathVariable Integer id) {
        Integer idUsuario = obtenerIdUsuarioAutenticado();
        ofertaService.eliminar(id, idUsuario);
        return ResponseEntity.noContent().build();
    }
}