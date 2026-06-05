package com.contrabajo.servicios_api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos requeridos para asociar una URL de foto a una oferta")
public class FotoRequestDTO {

    @Schema(description = "URL segura de la foto subida directamente a Cloudinary", example = "https://res.cloudinary.com/dl1jjb7lx/image/upload/v1717360500/oferta_prueba.jpg")
    private String url;

    // Getter y Setter manuales según tu estándar
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
}