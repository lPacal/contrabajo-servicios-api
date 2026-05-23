package com.contrabajo.servicios_api.controller;

import com.contrabajo.servicios_api.dto.CitaServicioResponseDTO;
import com.contrabajo.servicios_api.dto.SolicitarCitaDTO;
import com.contrabajo.servicios_api.service.CitaServicioService;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@Tag(name = "4. Citas de servicio", description = "Endpoints para solicitar citas y avanzar el flujo de estados cliente-trabajador")
@RequestMapping("/api/citas")
@RequiredArgsConstructor
public class CitaServicioController {

    private final CitaServicioService citaService;
    private final JwtUtil jwtUtil;
    private final HttpServletRequest request;

    private Integer obtenerIdUsuarioAutenticado() {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return jwtUtil.extractId(authHeader.substring(7));
        }
        throw new RuntimeException("Acceso denegado: Token invalido.");
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 1. SOLICITAR — cliente crea la cita en PENDIENTE
    // ──────────────────────────────────────────────────────────────────────────
    @PostMapping("/solicitar")
    @PreAuthorize("hasAnyRole('CLIENTE','PREMIUM')")
    @Operation(
            summary = "Solicitar cita",
            description = "**Requiere rol CLIENTE o PREMIUM (BearerAuth)**<br><br>" +
                    "Crea una cita en estado pendiente para una oferta de servicio. El cliente solicitante se obtiene desde el token."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cita solicitada correctamente.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CitaServicioResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Oferta invalida, token ausente o regla de negocio incumplida.",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"error\": \"Acceso denegado: Token invalido.\"}"))),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente, invalido o expirado.", content = @Content),
            @ApiResponse(responseCode = "403", description = "Rol no autorizado para solicitar citas.", content = @Content)
    })
    public ResponseEntity<?> solicitar(@RequestBody SolicitarCitaDTO dto) {
        try {
            return ResponseEntity.status(201)
                    .body(citaService.solicitarServicio(dto, obtenerIdUsuarioAutenticado()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 2. ACEPTAR — PENDIENTE → HANDSHAKE (trabajador)
    // ──────────────────────────────────────────────────────────────────────────
    @PatchMapping("/{id}/aceptar")
    @PreAuthorize("hasAnyRole('TRABAJADOR','PREMIUM')")
    @Operation(summary = "Aceptar cita", description = "**Requiere rol TRABAJADOR o PREMIUM (BearerAuth)**<br><br>Avanza una cita pendiente a estado de handshake.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cita aceptada correctamente.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CitaServicioResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Cita inexistente, estado invalido o usuario sin permisos.", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"error\": \"Acceso denegado\"}"))),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente, invalido o expirado.", content = @Content),
            @ApiResponse(responseCode = "403", description = "Rol no autorizado para aceptar citas.", content = @Content)
    })
    public ResponseEntity<?> aceptar(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(citaService.aceptarCita(id, obtenerIdUsuarioAutenticado()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 3. RECHAZAR — PENDIENTE → RECHAZADA (trabajador)
    // ──────────────────────────────────────────────────────────────────────────
    @PatchMapping("/{id}/rechazar")
    @PreAuthorize("hasAnyRole('TRABAJADOR','PREMIUM')")
    @Operation(summary = "Rechazar cita", description = "**Requiere rol TRABAJADOR o PREMIUM (BearerAuth)**<br><br>Rechaza una cita pendiente y la deja disponible para reenvio de propuesta.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cita rechazada correctamente.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CitaServicioResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Cita inexistente, estado invalido o usuario sin permisos.", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"error\": \"Acceso denegado\"}"))),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente, invalido o expirado.", content = @Content),
            @ApiResponse(responseCode = "403", description = "Rol no autorizado para rechazar citas.", content = @Content)
    })
    public ResponseEntity<?> rechazar(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(citaService.rechazarCita(id, obtenerIdUsuarioAutenticado()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 4. REENVIAR PROPUESTA — RECHAZADA → PENDIENTE (cliente)
    // ──────────────────────────────────────────────────────────────────────────
    @PatchMapping("/{id}/reenviar")
    @PreAuthorize("hasAnyRole('CLIENTE','PREMIUM')")
    @Operation(summary = "Reenviar propuesta de cita", description = "**Requiere rol CLIENTE o PREMIUM (BearerAuth)**<br><br>Devuelve una cita rechazada al estado pendiente para que el trabajador pueda revisarla nuevamente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Propuesta reenviada correctamente.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CitaServicioResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Cita inexistente, estado invalido o usuario sin permisos.", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"error\": \"Acceso denegado\"}"))),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente, invalido o expirado.", content = @Content),
            @ApiResponse(responseCode = "403", description = "Rol no autorizado para reenviar citas.", content = @Content)
    })
    public ResponseEntity<?> reenviar(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(citaService.reenviarPropuesta(id, obtenerIdUsuarioAutenticado()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 5. COMENZAR — HANDSHAKE → COMENZANDO (trabajador solicita inicio)
    // ──────────────────────────────────────────────────────────────────────────
    @PatchMapping("/{id}/comenzar")
    @PreAuthorize("hasAnyRole('TRABAJADOR','PREMIUM')")
    @Operation(summary = "Solicitar inicio de trabajo", description = "**Requiere rol TRABAJADOR o PREMIUM (BearerAuth)**<br><br>El trabajador solicita comenzar el trabajo y la cita pasa a espera de confirmacion del cliente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Solicitud de inicio registrada.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CitaServicioResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Cita inexistente, estado invalido o usuario sin permisos.", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"error\": \"Acceso denegado\"}"))),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente, invalido o expirado.", content = @Content),
            @ApiResponse(responseCode = "403", description = "Rol no autorizado para iniciar trabajo.", content = @Content)
    })
    public ResponseEntity<?> comenzar(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(citaService.comenzarTrabajo(id, obtenerIdUsuarioAutenticado()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 6. CONFIRMAR INICIO — COMENZANDO → EN_PROCESO (cliente confirma)
    // ──────────────────────────────────────────────────────────────────────────
    @PatchMapping("/{id}/confirmar-inicio")
    @PreAuthorize("hasAnyRole('CLIENTE','PREMIUM')")
    @Operation(summary = "Confirmar inicio de trabajo", description = "**Requiere rol CLIENTE o PREMIUM (BearerAuth)**<br><br>El cliente confirma que el trabajo comenzo y la cita pasa a estado en proceso.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Inicio confirmado correctamente.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CitaServicioResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Cita inexistente, estado invalido o usuario sin permisos.", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"error\": \"Acceso denegado\"}"))),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente, invalido o expirado.", content = @Content),
            @ApiResponse(responseCode = "403", description = "Rol no autorizado para confirmar inicio.", content = @Content)
    })
    public ResponseEntity<?> confirmarInicio(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(citaService.confirmarInicio(id, obtenerIdUsuarioAutenticado()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 7. FINALIZAR — EN_PROCESO → FINALIZANDO (trabajador solicita cierre)
    // ──────────────────────────────────────────────────────────────────────────
    @PatchMapping("/{id}/finalizar")
    @PreAuthorize("hasAnyRole('TRABAJADOR','PREMIUM')")
    @Operation(summary = "Solicitar finalizacion de trabajo", description = "**Requiere rol TRABAJADOR o PREMIUM (BearerAuth)**<br><br>El trabajador solicita finalizar el trabajo y la cita pasa a espera de confirmacion del cliente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Solicitud de finalizacion registrada.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CitaServicioResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Cita inexistente, estado invalido o usuario sin permisos.", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"error\": \"Acceso denegado\"}"))),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente, invalido o expirado.", content = @Content),
            @ApiResponse(responseCode = "403", description = "Rol no autorizado para finalizar trabajo.", content = @Content)
    })
    public ResponseEntity<?> finalizar(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(citaService.finalizarTrabajo(id, obtenerIdUsuarioAutenticado()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 8. CONFIRMAR FINALIZACION — FINALIZANDO → FINALIZADO (cliente confirma)
    // ──────────────────────────────────────────────────────────────────────────
    @PatchMapping("/{id}/confirmar-finalizacion")
    @PreAuthorize("hasAnyRole('CLIENTE','PREMIUM')")
    @Operation(summary = "Confirmar finalizacion de trabajo", description = "**Requiere rol CLIENTE o PREMIUM (BearerAuth)**<br><br>El cliente confirma que el trabajo finalizo y la cita pasa a estado finalizado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Finalizacion confirmada correctamente.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CitaServicioResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Cita inexistente, estado invalido o usuario sin permisos.", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"error\": \"Acceso denegado\"}"))),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente, invalido o expirado.", content = @Content),
            @ApiResponse(responseCode = "403", description = "Rol no autorizado para confirmar finalizacion.", content = @Content)
    })
    public ResponseEntity<?> confirmarFinalizacion(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(citaService.confirmarFinalizacion(id, obtenerIdUsuarioAutenticado()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 9. CANCELAR — cualquier estado activo → CANCELADO (cualquiera)
    // ──────────────────────────────────────────────────────────────────────────
    @PatchMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar cita", description = "**Requiere Token JWT valido (BearerAuth)**<br><br>Cancela una cita activa. Puede ejecutarlo un participante autorizado de la cita.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cita cancelada correctamente.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CitaServicioResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Cita inexistente, estado invalido o usuario sin permisos.", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"error\": \"Acceso denegado\"}"))),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente, invalido o expirado.", content = @Content),
            @ApiResponse(responseCode = "403", description = "Rol no autorizado por la configuracion de seguridad.", content = @Content)
    })
    public ResponseEntity<?> cancelar(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(citaService.cancelarCita(id, obtenerIdUsuarioAutenticado()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 10. LISTAR MIS CITAS
    // ──────────────────────────────────────────────────────────────────────────
    @GetMapping("/mis-citas")
    @Operation(summary = "Listar mis citas", description = "**Requiere Token JWT valido (BearerAuth)**<br><br>Devuelve todas las citas donde participa el usuario autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de citas del usuario.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CitaServicioResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente, invalido o expirado.", content = @Content),
            @ApiResponse(responseCode = "403", description = "Rol no autorizado por la configuracion de seguridad.", content = @Content)
    })
    public ResponseEntity<List<CitaServicioResponseDTO>> misCitas() {
        return ResponseEntity.ok(citaService.listarMisCitas(obtenerIdUsuarioAutenticado()));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 11. DETALLE DE UNA CITA
    // ──────────────────────────────────────────────────────────────────────────
    @GetMapping("/{id}")
    @Operation(summary = "Obtener detalle de cita", description = "**Requiere Token JWT valido (BearerAuth)**<br><br>Devuelve el detalle de una cita. Solo sus participantes autorizados pueden verla.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Detalle de cita encontrado.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CitaServicioResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Cita inexistente o usuario sin permisos.", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"error\": \"Acceso denegado\"}"))),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente, invalido o expirado.", content = @Content),
            @ApiResponse(responseCode = "403", description = "Rol no autorizado por la configuracion de seguridad.", content = @Content)
    })
    public ResponseEntity<?> detalle(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(citaService.obtenerCita(id, obtenerIdUsuarioAutenticado()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
