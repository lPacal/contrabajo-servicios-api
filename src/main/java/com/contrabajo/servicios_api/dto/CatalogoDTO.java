package com.contrabajo.servicios_api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "Elemento generico de catalogo usado por categorias, tipos de precio y listas simples.")
public class CatalogoDTO {
    @Schema(description = "ID del elemento de catalogo.", example = "1")
    private Integer id;

    @Schema(description = "Nombre visible del elemento de catalogo.", example = "Reparaciones")
    private String nombre;
}
