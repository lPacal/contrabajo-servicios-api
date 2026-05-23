package com.contrabajo.servicios_api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "Oferta de servicio enriquecida para visualizacion del cliente.")
public class OfertaServicioResponseDTO {
    @Schema(description = "ID unico de la oferta.", example = "100")
    private Integer id;

    @Schema(description = "Titulo visible de la oferta.", example = "Reparacion de lavadora")
    private String titulo;

    @Schema(description = "Descripcion detallada de la oferta.", example = "Servicio tecnico a domicilio para lavadoras.")
    private String descripcion;

    @Schema(description = "Precio base de la oferta.", example = "45000")
    private BigDecimal precio;

    @Schema(description = "Indica si la oferta esta disponible para nuevas citas.", example = "true")
    private Boolean disponible;

    @Schema(description = "Indica si la oferta fue eliminada logicamente.", example = "false")
    private Boolean borrado;

    @Schema(description = "Fecha de publicacion de la oferta.")
    private LocalDateTime fechaPublicacion;

    @Schema(description = "ID del trabajador propietario.", example = "8")
    private Integer idTrabajador;

    @Schema(description = "ID de la categoria.", example = "1")
    private Integer idCategoria;

    @Schema(description = "ID del tipo de precio.", example = "1")
    private Integer idTipoPrecio;

    @Schema(description = "Rango de disponibilidad en metros.", example = "5000")
    private Integer rangoDisponibilidadM;

    @Schema(description = "Texto de ubicacion de referencia.", example = "Santiago Centro")
    private String ubicacionReferencia;

    @Schema(description = "Latitud de referencia de la oferta.", example = "-33.4489")
    private Double latitudReferencia;

    @Schema(description = "Longitud de referencia de la oferta.", example = "-70.6693")
    private Double longitudReferencia;
    
    // Devolvemos los nombres directamente para hacerle la vida fácil al frontend
    @Schema(description = "Nombre de la categoria.", example = "Reparaciones")
    private String categoria;

    @Schema(description = "Nombre del tipo de precio.", example = "Por servicio")
    private String tipoPrecio;

    @Schema(description = "Nombre visible del trabajador.", example = "Trabajador Prueba")
    private String nombreTrabajador;

    @Schema(description = "Username del trabajador.", example = "trabajador_prueba")
    private String usernameTrabajador;
}
