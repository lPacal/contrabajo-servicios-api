package com.contrabajo.servicios_api.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class OfertaServicioUpdateDTO {
    private String titulo;
    private String descripcion;
    private BigDecimal precio;
    private Boolean disponible;
    private Integer idCategoria;
    private Integer idTipoPrecio;
}