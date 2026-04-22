package com.contrabajo.servicios_api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "oferta_servicio", schema = "dbo")
@Getter
@Setter
@NoArgsConstructor
public class OfertaServicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_oferta_servicio")
    private Integer id;

    @Column(nullable = false, length = 80)
    private String titulo;

    @Column(nullable = false, length = 300)
    private String descripcion;

    // Se usa BigDecimal para manejar dinero y precisión decimal correctamente
    @Column(precision = 12, scale = 2)
    private BigDecimal precio;

    @Column(nullable = false)
    private Boolean disponible = true; // El script de BD dice DEFAULT 1

    // NUEVO CAMPO: Para el borrado lógico (Soft Delete)
    @Column(nullable = false)
    private Boolean borrado = false;

    @Column(name = "fecha_publicacion", insertable = false, updatable = false)
    private LocalDateTime fechaPublicacion;

    // =========================================================
    // RELACIONES FÍSICAS (Con tablas de esta misma Base de Datos)
    // =========================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cat_servicio", nullable = false)
    private CategoriaServicio categoriaServicio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_precio")
    private TipoPrecio tipoPrecio;

    // =========================================================
    // RELACIONES LÓGICAS (Hacia el MS_Usuarios)
    // No usamos @ManyToOne para mantener el desacoplamiento
    // =========================================================

    @Column(name = "id_trabajador", nullable = false)
    private Integer idTrabajador;

    @Column(name = "id_cliente")
    private Integer idCliente;
}