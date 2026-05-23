package com.contrabajo.servicios_api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Metadatos de una foto asociada a una oferta de servicio.")
public class FotoResponseDTO {

    @Schema(description = "ID unico de la foto.", example = "12")
    @JsonProperty("id_foto")
    private Integer idFoto;

    @Schema(description = "URL publica o ruta de acceso a la imagen.", example = "/uploads/ofertas/12.webp")
    @JsonProperty("enlace")
    private String enlace;

    @Schema(description = "Nombre original del archivo cargado.", example = "lavadora.webp")
    @JsonProperty("nombre_original")
    private String nombreOriginal;

    @Schema(description = "Tipo MIME detectado para la imagen.", example = "image/webp")
    @JsonProperty("tipo_mime")
    private String tipoMime;

    @Schema(description = "Tamano del archivo en bytes.", example = "245000")
    @JsonProperty("tamano_bytes")
    private Long tamanoBytes;

    @Schema(description = "Ancho de la imagen en pixeles.", example = "1280")
    @JsonProperty("ancho_px")
    private Integer anchoPx;

    @Schema(description = "Alto de la imagen en pixeles.", example = "720")
    @JsonProperty("alto_px")
    private Integer altoPx;

    @Schema(description = "Fecha en que la imagen fue subida.")
    @JsonProperty("fecha_subida")
    private LocalDateTime fechaSubida;

    @Schema(description = "ID de la oferta de servicio asociada.", example = "100")
    @JsonProperty("id_oferta_servicio")
    private Integer idOfertaServicio;

    @Schema(description = "ID del usuario propietario de la oferta.", example = "8")
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
