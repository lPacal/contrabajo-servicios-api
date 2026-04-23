package com.contrabajo.servicios_api.dto;

import lombok.Data;

@Data
public class SolicitarCitaDTO {
    private Integer idOfertaServicio;
    private String comentario; // Ej: "Necesito arreglar la tubería del lavaplatos, ¿tienes disponibilidad el martes?"
    private Integer idCoordenadas; // Opcional, si la app manda su ubicación actual
}