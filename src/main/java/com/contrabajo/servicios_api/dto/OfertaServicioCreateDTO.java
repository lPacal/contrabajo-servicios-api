package com.contrabajo.servicios_api.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class OfertaServicioCreateDTO {
    private String titulo;
    private String descripcion;
    private BigDecimal precio; 
    private Integer idCategoria;
    private Integer idTipoPrecio;
}