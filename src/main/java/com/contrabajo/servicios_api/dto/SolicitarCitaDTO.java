package com.contrabajo.servicios_api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Solicitud para crear una cita asociada a una oferta de servicio.")
public class SolicitarCitaDTO {
    @Schema(description = "ID de la oferta de servicio solicitada.", example = "100", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer idOfertaServicio;

    @Schema(description = "Comentario del cliente para coordinar el servicio.", example = "Disponible el viernes por la tarde.")
    private String comentario;

    @Schema(description = "ID de coordenadas del cliente, si aplica.", example = "15")
    private Integer idCoordenadas; // Opcional

    @Schema(description = "ID del chat asociado, si la cita nace desde una conversacion.", example = "10")
    private Long idChatOferta;     // Opcional — vincula la cita al chat
}
