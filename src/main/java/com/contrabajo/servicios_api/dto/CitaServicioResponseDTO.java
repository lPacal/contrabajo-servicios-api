package com.contrabajo.servicios_api.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CitaServicioResponseDTO {
    private Integer id;
    private String comentario;
    private LocalDateTime fechaSolicitud;
    private LocalDateTime fechaInicioTrabajo;
    private LocalDateTime fechaFinTrabajo;

    // Datos planos de la relación para no enviar objetos gigantes
    private Integer idOfertaServicio;
    private String tituloOferta;
    private Integer idCliente;
    private Integer idTrabajador;

    // Estado: ID numerico (401-409), codigo ("CITA_PENDIENTE") y nombre legible ("Pendiente")
    private Integer idEstado;
    private String codigoEstado;
    private String estado;
}