package com.contrabajo.servicios_api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Datos de usuario y ubicacion obtenidos desde usuarios-api para enriquecer ofertas.")
public class UsuarioUbicacionDTO {
    @Schema(description = "ID del usuario.", example = "8")
    private Integer id;

    @Schema(description = "Username del usuario.", example = "trabajador_prueba")
    private String username;

    @Schema(description = "Rango de disponibilidad laboral en metros.", example = "5000")
    @JsonProperty("rango_disponibilidad_m")
    private Integer rangoDisponibilidadM;

    @Schema(description = "Direccion principal del usuario.")
    private DireccionUbicacionDTO direccion;

    @Schema(description = "Nombre del usuario.", example = "Trabajador")
    private String nombre;

    @Schema(description = "Apellido paterno del usuario.", example = "Prueba")
    @JsonProperty("apellidos")
    private String apellidoPaterno;

    @Data
    @Schema(description = "Direccion georreferenciada del usuario.")
    public static class DireccionUbicacionDTO {
        @Schema(description = "Calle de la direccion.", example = "Avenida Siempre Viva")
        private String calle;

        @Schema(description = "Numero de la direccion.", example = "742")
        private String numero;

        @Schema(description = "Comuna asociada a la direccion.")
        private ComunaUbicacionDTO comuna;

        @Schema(description = "Latitud de la direccion.", example = "-33.4489")
        private BigDecimal latitud;

        @Schema(description = "Longitud de la direccion.", example = "-70.6693")
        private BigDecimal longitud;
    }

    @Data
    @Schema(description = "Comuna y region asociadas a una direccion.")
    public static class ComunaUbicacionDTO {
        @Schema(description = "ID de la comuna.", example = "13101")
        private Integer id;

        @Schema(description = "Nombre de la comuna.", example = "Santiago")
        private String nombre;

        @Schema(description = "ID de la region.", example = "13")
        private Integer idRegion;

        @Schema(description = "Nombre de la region.", example = "Region Metropolitana")
        private String region;
    }
}
