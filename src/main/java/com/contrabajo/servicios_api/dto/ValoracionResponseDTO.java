package com.contrabajo.servicios_api.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ValoracionResponseDTO {
    private Long id;
    private Integer idCita;
    private Integer idCliente;
    private Integer idTrabajador;
    private Short voto;
    private String comentario;
    private LocalDateTime fechaVoto;
}