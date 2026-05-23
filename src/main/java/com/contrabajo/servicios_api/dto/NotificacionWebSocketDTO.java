package com.contrabajo.servicios_api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Notificacion enviada por WebSocket para eventos relevantes del modulo de servicios.")
public class NotificacionWebSocketDTO {
    @Schema(description = "Tipo tecnico de evento WebSocket.", example = "NUEVO_SERVICIO")
    private String tipo; // Ej: "NUEVO_SERVICIO", "SERVICIO_TOMADO"

    @Schema(description = "ID del servicio relacionado con el evento.", example = "100")
    private Long idServicio;

    @Schema(description = "Mensaje legible de la notificacion.", example = "Se ha solicitado una nueva cita.")
    private String mensaje;

    @Schema(description = "Fecha de emision de la notificacion.")
    private LocalDateTime fecha = LocalDateTime.now();
}
