package com.contrabajo.servicios_api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cita_servicio", schema = "dbo")
@Getter
@Setter
@NoArgsConstructor
public class CitaServicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cita")
    private Integer id;

    @Column(length = 200)
    private String comentario;

    @Column(name = "precio_acordado", precision = 12, scale = 2)
    private BigDecimal precioAcordado;

    @Column(name = "fecha_solicitud", insertable = false, updatable = false)
    private LocalDateTime fechaSolicitud;

    @Column(name = "fecha_inicio_trabajo")
    private LocalDateTime fechaInicioTrabajo;

    @Column(name = "fecha_fin_trabajo")
    private LocalDateTime fechaFinTrabajo;

    @Column(name = "cod_inicio")
    private Integer codInicio;

    @Column(name = "cod_final")
    private Integer codFinal;

    // =========================================================
    // RELACIONES FÍSICAS (Con tablas de MS_Servicios)
    // =========================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_oferta_servicio", nullable = false)
    private OfertaServicio ofertaServicio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cat_servicio", nullable = false)
    private CategoriaServicio categoriaServicio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_estado", nullable = false)
    private Estado estado;

    // =========================================================
    // RELACIONES LÓGICAS (Hacia MS_Usuarios)
    // =========================================================

    @Column(name = "id_coordenadas")
    private Integer idCoordenadas;

    @Column(name = "id_trabajador", nullable = false)
    private Integer idTrabajador;

    @Column(name = "id_cliente", nullable = false)
    private Integer idCliente;
}