package com.contrabajo.servicios_api.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OfertaServicioResponseDTO {
    private Integer id;
    private String titulo;
    private String descripcion;
    private BigDecimal precio;
    private Boolean disponible;
    private LocalDateTime fechaPublicacion;
    private Integer idTrabajador;
    
    // Devolvemos los nombres directamente para hacerle la vida fácil al frontend
    private String categoria; 
    private String tipoPrecio;
}