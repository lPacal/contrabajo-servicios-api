package com.contrabajo.servicios_api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "Detalle de una cita de servicio y su estado operativo.")
public class CitaServicioResponseDTO {
    @Schema(description = "ID unico de la cita.", example = "77")
    private Integer id;

    @Schema(description = "Comentario ingresado al solicitar la cita.", example = "Necesito el servicio durante la tarde.")
    private String comentario;

    @Schema(description = "Fecha en que el cliente solicito la cita.")
    private LocalDateTime fechaSolicitud;

    @Schema(description = "Fecha en que se inicio el trabajo, si aplica.", nullable = true)
    private LocalDateTime fechaInicioTrabajo;

    @Schema(description = "Fecha en que finalizo el trabajo, si aplica.", nullable = true)
    private LocalDateTime fechaFinTrabajo;

    // Datos planos de la relación para no enviar objetos gigantes
    @Schema(description = "ID de la oferta asociada a la cita.", example = "100")
    private Integer idOfertaServicio;

    @Schema(description = "Titulo de la oferta asociada.", example = "Reparacion de lavadora")
    private String tituloOferta;

    @Schema(description = "ID del cliente solicitante.", example = "5")
    private Integer idCliente;

    @Schema(description = "ID del trabajador dueño de la oferta.", example = "8")
    private Integer idTrabajador;

    // Estado: ID numerico (401-409), codigo ("CITA_PENDIENTE") y nombre legible ("Pendiente")
    @Schema(description = "ID numerico del estado de cita.", example = "401")
    private Integer idEstado;

    @Schema(description = "Codigo tecnico del estado de cita.", example = "CITA_PENDIENTE")
    private String codigoEstado;

    @Schema(description = "Nombre legible del estado de cita.", example = "Pendiente")
    private String estado;
}
