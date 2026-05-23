package com.contrabajo.servicios_api.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Datos requeridos para crear una oferta de servicio.")
public class OfertaServicioCreateDTO {
    @Schema(description = "Titulo visible de la oferta.", example = "Reparacion de lavadora", requiredMode = Schema.RequiredMode.REQUIRED)
    private String titulo;

    @Schema(description = "Descripcion detallada del servicio ofrecido.", example = "Servicio tecnico a domicilio para lavadoras.")
    private String descripcion;

    @Schema(description = "Precio base de la oferta.", example = "45000")
    private BigDecimal precio; 

    @Schema(description = "ID de la categoria de servicio.", example = "1")
    private Integer idCategoria;

    @Schema(description = "ID del tipo de precio.", example = "1")
    private Integer idTipoPrecio;
}
