package com.contrabajo.servicios_api.dto;

import lombok.Data;

@Data
public class SolicitarCitaDTO {
    private Integer idOfertaServicio;
    private String comentario;
    private Integer idCoordenadas; // Opcional
    private Long idChatOferta;     // Opcional — vincula la cita al chat
}