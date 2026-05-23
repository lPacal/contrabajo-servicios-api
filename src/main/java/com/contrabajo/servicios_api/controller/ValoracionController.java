package com.contrabajo.servicios_api.controller;

import com.contrabajo.servicios_api.dto.ValoracionRequestDTO;
import com.contrabajo.servicios_api.dto.ValoracionResponseDTO;
import com.contrabajo.servicios_api.service.ValoracionService;
import com.contrabajo.servicios_api.utils.JwtUtil; // Asegúrate de importar tu JwtUtil
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
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@Tag(name = "6. Valoraciones", description = "Endpoints para registrar y consultar valoraciones de servicios")
@RequestMapping("/api/valoraciones")
@RequiredArgsConstructor
public class ValoracionController {

    private final ValoracionService valoracionService;
    private final JwtUtil jwtUtil; // Inyectamos la herramienta para leer tokens

    @PostMapping
    @Operation(
            summary = "Crear valoracion",
            description = "**Requiere Token JWT valido (BearerAuth)**<br><br>" +
                    "Registra la valoracion de una cita finalizada. El cliente autenticado se obtiene desde el token."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Valoracion registrada correctamente.",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"mensaje\": \"La valoración ha sido registrada con éxito.\"}"))),
            @ApiResponse(responseCode = "400", description = "Token ausente, cita invalida o regla de negocio incumplida.",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"error\": \"Token de autorización no encontrado.\"}"))),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente, invalido o expirado.", content = @Content),
            @ApiResponse(responseCode = "403", description = "Rol no autorizado por la configuracion de seguridad.", content = @Content)
    })
    public ResponseEntity<?> crearValoracion(@RequestBody ValoracionRequestDTO dto, HttpServletRequest request) {
        try {
            // 1. Capturamos el token de la cabecera
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new RuntimeException("Token de autorización no encontrado.");
            }
            String token = authHeader.substring(7); // Quitamos la palabra "Bearer "

            // 2. Extraemos el ID del cliente directamente de la "maleta" del token
            Integer idClienteAutenticado = jwtUtil.extractId(token);
            
            // 3. Pasamos el ID al Service
            valoracionService.crearValoracion(dto, idClienteAutenticado);
            
            return ResponseEntity.ok(Map.of("mensaje", "La valoración ha sido registrada con éxito."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/trabajador/{idTrabajador}")
    @Operation(
            summary = "Listar valoraciones por trabajador",
            description = "**Requiere Token JWT valido (BearerAuth)**<br><br>" +
                    "Devuelve las valoraciones recibidas por un trabajador. El token se usa solo para validar acceso autenticado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de valoraciones del trabajador.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ValoracionResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Token ausente o error al consultar valoraciones.",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"error\": \"Token de autorización no encontrado.\"}"))),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente, invalido o expirado.", content = @Content),
            @ApiResponse(responseCode = "403", description = "Rol no autorizado por la configuracion de seguridad.", content = @Content)
    })
    public ResponseEntity<?> obtenerPorTrabajador(@PathVariable Integer idTrabajador, HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new RuntimeException("Token de autorización no encontrado.");
            }
            String token = authHeader.substring(7);
            // Solo exigimos token valido; las valoraciones del trabajador son publicas para clientes.
            jwtUtil.extractId(token);
            return ResponseEntity.ok(valoracionService.obtenerPorTrabajador(idTrabajador));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/cliente/{idCliente}")
    @Operation(
            summary = "Listar valoraciones por cliente",
            description = "**Requiere Token JWT valido (BearerAuth)**<br><br>" +
                    "Devuelve las valoraciones emitidas por un cliente. El ID de la ruta debe coincidir con el ID del token."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de valoraciones del cliente.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ValoracionResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Token ausente o intento de consultar valoraciones de otro cliente.",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"error\": \"No tienes permiso para consultar estas valoraciones.\"}"))),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente, invalido o expirado.", content = @Content),
            @ApiResponse(responseCode = "403", description = "Rol no autorizado por la configuracion de seguridad.", content = @Content)
    })
    public ResponseEntity<?> obtenerPorCliente(@PathVariable Integer idCliente, HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new RuntimeException("Token de autorización no encontrado.");
            }
            String token = authHeader.substring(7);
            Integer idUsuarioAutenticado = jwtUtil.extractId(token);
            if (!idCliente.equals(idUsuarioAutenticado)) {
                throw new RuntimeException("No tienes permiso para consultar estas valoraciones.");
            }
            return ResponseEntity.ok(valoracionService.obtenerPorCliente(idCliente));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
