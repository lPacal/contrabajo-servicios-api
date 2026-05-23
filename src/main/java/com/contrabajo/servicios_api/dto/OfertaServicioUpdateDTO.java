package com.contrabajo.servicios_api.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Datos opcionales para actualizar una oferta de servicio.")
public class OfertaServicioUpdateDTO {
    @Schema(description = "Nuevo titulo de la oferta.", example = "Reparacion integral de lavadora")
    private String titulo;

    @Schema(description = "Nueva descripcion de la oferta.", example = "Incluye diagnostico y reparacion basica.")
    private String descripcion;

    @Schema(description = "Nuevo precio base.", example = "50000")
    private BigDecimal precio;

    @Schema(description = "Nuevo estado de disponibilidad.", example = "true")
    private Boolean disponible;

    @Schema(description = "Nuevo ID de categoria.", example = "1")
    private Integer idCategoria;

    @Schema(description = "Nuevo ID de tipo de precio.", example = "1")
    private Integer idTipoPrecio;
}
