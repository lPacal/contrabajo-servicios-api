package com.contrabajo.servicios_api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Resultado del registro o consulta de una foto asociada a una oferta de servicio")
public class FotoResponseDTO {

    @JsonProperty("id_foto")
    @Schema(description = "ID interno de la foto de la oferta", example = "12")
    private Integer idFoto;

    @JsonProperty("enlace")
    @Schema(description = "URL pública de acceso a la imagen en Cloudinary", example = "https://res.cloudinary.com/dl1jjb7lx/image/upload/v1717360500/oferta_prueba.jpg")
    private String enlace;

    @JsonProperty("fecha_subida")
    @Schema(description = "Fecha y hora del registro en base de datos", example = "2026-06-04T19:00:00")
    private LocalDateTime fechaSubida;

    @JsonProperty("id_oferta_servicio")
    @Schema(description = "ID de la oferta de servicio asociada", example = "1")
    private Integer idOfertaServicio;

    @JsonProperty("id_usuario")
    @Schema(description = "ID del trabajador que vinculó la foto", example = "3")
    private Integer idUsuario;

    // Constructor optimizado según tu estándar
    public FotoResponseDTO(Integer idFoto, String enlace, LocalDateTime fechaSubida, 
                           Integer idOfertaServicio, Integer idUsuario) {
        this.idFoto = idFoto;
        this.enlace = enlace;
        this.fechaSubida = fechaSubida;
        this.idOfertaServicio = idOfertaServicio;
        this.idUsuario = idUsuario;
    }

    // Getters manuales explícitos
    public Integer getIdFoto()           { return idFoto; }
    public String getEnlace()            { return enlace; }
    public LocalDateTime getFechaSubida() { return fechaSubida; }
    public Integer getIdOfertaServicio() { return idOfertaServicio; }
    public Integer getIdUsuario()        { return idUsuario; }
}