package com.contrabajo.servicios_api.dto;

import lombok.Data;

@Data
public class ValoracionRequestDTO {
    private Integer idCita; 
    private Short voto; // Coincide con tu entidad
    private String comentario;
}