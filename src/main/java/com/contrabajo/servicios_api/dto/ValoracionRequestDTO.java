package com.contrabajo.servicios_api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Solicitud para registrar una valoracion sobre una cita finalizada.")
public class ValoracionRequestDTO {
    @Schema(description = "ID de la cita valorada.", example = "77", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer idCita; 

    @Schema(description = "Voto numerico entregado por el cliente.", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    private Short voto; // Coincide con tu entidad

    @Schema(description = "Comentario opcional de la valoracion.", example = "Excelente servicio y puntualidad.")
    private String comentario;
}
