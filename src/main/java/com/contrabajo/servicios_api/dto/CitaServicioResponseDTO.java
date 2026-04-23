package com.contrabajo.servicios_api.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CitaServicioResponseDTO {
    private Integer id;
    private String comentario;
    private LocalDateTime fechaSolicitud;
    
    // Datos planos de la relación para no enviar objetos gigantes
    private Integer idOfertaServicio;
    private String tituloOferta;
    private Integer idCliente;
    private Integer idTrabajador;
    
    // El estado actual ("Pendiente", "Aceptado", etc.)
    private String estado; 
}