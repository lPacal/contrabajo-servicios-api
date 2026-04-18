package com.contrabajo.servicios_api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificacionWebSocketDTO {
    private String tipo; // Ej: "NUEVO_SERVICIO", "SERVICIO_TOMADO"
    private Long idServicio;
    private String mensaje;
    private LocalDateTime fecha = LocalDateTime.now();
}