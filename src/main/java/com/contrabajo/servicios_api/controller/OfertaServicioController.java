package com.contrabajo.servicios_api.controller;

import com.contrabajo.servicios_api.dto.OfertaServicioCreateDTO;
import com.contrabajo.servicios_api.dto.OfertaServicioResponseDTO;
import com.contrabajo.servicios_api.dto.OfertaServicioUpdateDTO;
import com.contrabajo.servicios_api.service.OfertaServicioService;
import com.contrabajo.servicios_api.utils.JwtUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@Tag(name = "3. Ofertas de servicio", description = "Endpoints para publicar, consultar, actualizar y eliminar ofertas de servicio")
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
    @Operation(
            summary = "Crear oferta de servicio",
            description = "**Requiere rol TRABAJADOR o PREMIUM (BearerAuth)**<br><br>" +
                    "Publica una nueva oferta de servicio usando el ID del usuario autenticado como trabajador propietario."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Oferta creada correctamente.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = OfertaServicioResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos invalidos o regla de negocio incumplida.",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"error\": \"Categoría no encontrada\"}"))),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente, invalido o expirado.", content = @Content),
            @ApiResponse(responseCode = "403", description = "Rol no autorizado para crear ofertas.", content = @Content)
    })
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
    @Operation(summary = "Listar ofertas", description = "Devuelve todas las ofertas visibles para el cliente, enriquecidas con datos de categoria, precio y trabajador.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de ofertas.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = OfertaServicioResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente, invalido o expirado.", content = @Content),
            @ApiResponse(responseCode = "403", description = "Rol no autorizado por la configuracion de seguridad.", content = @Content)
    })
    public ResponseEntity<List<OfertaServicioResponseDTO>> listarTodas() {
        return ResponseEntity.ok(ofertaService.listarTodas(obtenerAuthorizationHeader()));
    }

    // Buscar por ID
    @GetMapping("/{id}")
    @Operation(summary = "Buscar oferta por ID", description = "Devuelve el detalle de una oferta de servicio especifica.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Oferta encontrada.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = OfertaServicioResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Oferta no encontrada.",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"error\": \"Oferta de servicio no encontrada\"}"))),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente, invalido o expirado.", content = @Content),
            @ApiResponse(responseCode = "403", description = "Rol no autorizado por la configuracion de seguridad.", content = @Content)
    })
    public ResponseEntity<?> buscarPorId(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(ofertaService.buscarPorId(id, obtenerAuthorizationHeader()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}/disponibilidad")
    @PreAuthorize("hasAnyRole('TRABAJADOR', 'PREMIUM')")
    @Operation(
            summary = "Obtener disponibilidad de oferta",
            description = "**Requiere rol TRABAJADOR o PREMIUM (BearerAuth)**<br><br>" +
                    "Consulta si la oferta esta disponible. Solo el propietario autenticado puede revisar este estado operativo."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Disponibilidad de la oferta.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Boolean.class))),
            @ApiResponse(responseCode = "400", description = "Oferta inexistente o usuario sin permisos.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Boolean.class))),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente, invalido o expirado.", content = @Content),
            @ApiResponse(responseCode = "403", description = "Rol no autorizado para consultar disponibilidad.", content = @Content)
    })
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
    @Operation(summary = "Listar ofertas por trabajador", description = "Devuelve las ofertas publicadas por un trabajador especifico.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de ofertas del trabajador.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = OfertaServicioResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente, invalido o expirado.", content = @Content),
            @ApiResponse(responseCode = "403", description = "Rol no autorizado por la configuracion de seguridad.", content = @Content)
    })
    public ResponseEntity<List<OfertaServicioResponseDTO>> listarPorTrabajador(@PathVariable Integer idTrabajador) {
        return ResponseEntity.ok(ofertaService.listarPorTrabajador(idTrabajador, obtenerAuthorizationHeader()));
    }

    // Actualizar oferta
    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('TRABAJADOR', 'PREMIUM')")
    @Operation(
            summary = "Actualizar oferta parcialmente",
            description = "**Requiere rol TRABAJADOR o PREMIUM (BearerAuth)**<br><br>" +
                    "Actualiza uno o mas campos de una oferta. Los campos omitidos mantienen su valor actual."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Oferta actualizada correctamente.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = OfertaServicioResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Oferta inexistente, datos invalidos o usuario sin permisos.",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"error\": \"Acceso denegado\"}"))),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente, invalido o expirado.", content = @Content),
            @ApiResponse(responseCode = "403", description = "Rol no autorizado para actualizar ofertas.", content = @Content)
    })
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
    @Operation(
            summary = "Actualizar oferta por PUT",
            description = "**Requiere rol TRABAJADOR o PREMIUM (BearerAuth)**<br><br>" +
                    "Endpoint de compatibilidad que reutiliza la misma logica de actualizacion parcial de la oferta."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Oferta actualizada correctamente.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = OfertaServicioResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Oferta inexistente, datos invalidos o usuario sin permisos.",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"error\": \"Acceso denegado\"}"))),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente, invalido o expirado.", content = @Content),
            @ApiResponse(responseCode = "403", description = "Rol no autorizado para actualizar ofertas.", content = @Content)
    })
    public ResponseEntity<?> actualizarOfertaPut(
            @PathVariable Integer id,
            @RequestBody OfertaServicioUpdateDTO dto) {
        return actualizarOferta(id, dto);
    }

    @PatchMapping("/{id}/disponibilidad/activar")
    @PreAuthorize("hasAnyRole('TRABAJADOR', 'PREMIUM')")
    @Operation(
            summary = "Activar disponibilidad de oferta",
            description = "**Requiere rol TRABAJADOR o PREMIUM (BearerAuth)**<br><br>" +
                    "Marca una oferta como disponible para recibir nuevas solicitudes."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Oferta activada correctamente.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = OfertaServicioResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Oferta inexistente o usuario sin permisos.",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"error\": \"Acceso denegado\"}"))),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente, invalido o expirado.", content = @Content),
            @ApiResponse(responseCode = "403", description = "Rol no autorizado para activar ofertas.", content = @Content)
    })
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
    @Operation(
            summary = "Desactivar disponibilidad de oferta",
            description = "**Requiere rol TRABAJADOR, PREMIUM, MODERADOR o ADMINISTRADOR (BearerAuth)**<br><br>" +
                    "Marca una oferta como no disponible. Moderadores y administradores pueden usarlo como medida de moderacion."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Oferta desactivada correctamente.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = OfertaServicioResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Oferta inexistente o usuario sin permisos.",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"error\": \"Acceso denegado\"}"))),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente, invalido o expirado.", content = @Content),
            @ApiResponse(responseCode = "403", description = "Rol no autorizado para desactivar ofertas.", content = @Content)
    })
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
    @Operation(
            summary = "Eliminar oferta",
            description = "**Requiere rol TRABAJADOR, PREMIUM, MODERADOR o ADMINISTRADOR (BearerAuth)**<br><br>" +
                    "Realiza el borrado logico de una oferta. Los roles de moderacion pueden eliminar ofertas por infraccion."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Oferta eliminada correctamente.",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"mensaje\": \"Oferta eliminada correctamente.\"}"))),
            @ApiResponse(responseCode = "400", description = "Oferta inexistente o usuario sin permisos.",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"error\": \"Acceso denegado\"}"))),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente, invalido o expirado.", content = @Content),
            @ApiResponse(responseCode = "403", description = "Rol no autorizado para eliminar ofertas.", content = @Content)
    })
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
