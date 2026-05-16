package com.contrabajo.servicios_api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UsuarioUbicacionDTO {
    private Integer id;
    private String username;
    @JsonProperty("rango_disponibilidad_m")
    private Integer rangoDisponibilidadM;
    private DireccionUbicacionDTO direccion;

    private String nombre;
    @JsonProperty("apellidos")
    private String apellidoPaterno;

    @Data
    public static class DireccionUbicacionDTO {
        private String calle;
        private String numero;
        private ComunaUbicacionDTO comuna;
        private BigDecimal latitud;
        private BigDecimal longitud;
    }

    @Data
    public static class ComunaUbicacionDTO {
        private Integer id;
        private String nombre;
        private Integer idRegion;
        private String region;
    }
}
