package com.contrabajo.servicios_api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "Valoracion registrada sobre una cita de servicio.")
public class ValoracionResponseDTO {
    @Schema(description = "ID unico de la valoracion.", example = "20")
    private Long id;

    @Schema(description = "ID de la cita valorada.", example = "77")
    private Integer idCita;

    @Schema(description = "ID de la oferta asociada.", example = "100")
    private Integer idOfertaServicio;

    @Schema(description = "ID del cliente que valoro.", example = "5")
    private Integer idCliente;

    @Schema(description = "ID del trabajador valorado.", example = "8")
    private Integer idTrabajador;

    @Schema(description = "Voto numerico registrado.", example = "5")
    private Short voto;

    @Schema(description = "Comentario de la valoracion.", example = "Excelente servicio y puntualidad.")
    private String comentario;

    @Schema(description = "Fecha en que se registro la valoracion.")
    private LocalDateTime fechaVoto;
}
