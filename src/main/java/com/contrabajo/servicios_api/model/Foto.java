package com.contrabajo.servicios_api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "foto", schema = "dbo")
@Getter
@Setter
public class Foto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_foto")
    private Integer id;

    @Column(name = "enlace", nullable = false, length = 300)
    private String enlace;

    @Column(name = "fecha_subida", nullable = false, insertable = false, updatable = false)
    private LocalDateTime fechaSubida;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_oferta_servicio", nullable = false)
    private OfertaServicio ofertaServicio;

    @Column(name = "id_usuario", nullable = false)
    private Integer idUsuario;
}