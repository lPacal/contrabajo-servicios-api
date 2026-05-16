package com.contrabajo.servicios_api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public class FotoResponseDTO {

    @JsonProperty("id_foto")
    private Integer idFoto;

    @JsonProperty("enlace")
    private String enlace;

    @JsonProperty("nombre_original")
    private String nombreOriginal;

    @JsonProperty("tipo_mime")
    private String tipoMime;

    @JsonProperty("tamano_bytes")
    private Long tamanoBytes;

    @JsonProperty("ancho_px")
    private Integer anchoPx;

    @JsonProperty("alto_px")
    private Integer altoPx;

    @JsonProperty("fecha_subida")
    private LocalDateTime fechaSubida;

    @JsonProperty("id_oferta_servicio")
    private Integer idOfertaServicio;

    @JsonProperty("id_usuario")
    private Integer idUsuario;

    // Constructor completo
    public FotoResponseDTO(Integer idFoto, String enlace, String nombreOriginal,
                           String tipoMime, Long tamanoBytes, Integer anchoPx, Integer altoPx,
                           LocalDateTime fechaSubida, Integer idOfertaServicio, Integer idUsuario) {
        this.idFoto = idFoto;
        this.enlace = enlace;
        this.nombreOriginal = nombreOriginal;
        this.tipoMime = tipoMime;
        this.tamanoBytes = tamanoBytes;
        this.anchoPx = anchoPx;
        this.altoPx = altoPx;
        this.fechaSubida = fechaSubida;
        this.idOfertaServicio = idOfertaServicio;
        this.idUsuario = idUsuario;
    }

    // Getters
    public Integer getIdFoto()            { return idFoto; }
    public String getEnlace()             { return enlace; }
    public String getNombreOriginal()     { return nombreOriginal; }
    public String getTipoMime()           { return tipoMime; }
    public Long getTamanoBytes()          { return tamanoBytes; }
    public Integer getAnchoPx()           { return anchoPx; }
    public Integer getAltoPx()            { return altoPx; }
    public LocalDateTime getFechaSubida() { return fechaSubida; }
    public Integer getIdOfertaServicio()  { return idOfertaServicio; }
    public Integer getIdUsuario()         { return idUsuario; }
}
