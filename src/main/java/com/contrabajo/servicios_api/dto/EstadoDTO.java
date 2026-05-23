package com.contrabajo.servicios_api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Estado catalogado para el flujo de citas de servicio.")
public class EstadoDTO {
    @Schema(description = "ID numerico del estado.", example = "401")
    private Integer id;

    @Schema(description = "Codigo tecnico del estado.", example = "CITA_PENDIENTE")
    private String codigo;

    @Schema(description = "Nombre visible del estado.", example = "Pendiente")
    private String nombre;

    @Schema(description = "Descripcion funcional del estado.", example = "La cita fue solicitada y espera respuesta del trabajador.")
    private String descripcion;

    public EstadoDTO() {
    }

    public EstadoDTO(Integer id, String codigo, String nombre, String descripcion) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
