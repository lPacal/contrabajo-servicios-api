package com.contrabajo.servicios_api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "valoracion", schema = "dbo")
@Getter
@Setter
@NoArgsConstructor
public class Valoracion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_valoracion")
    private Long id; // Como es BIGINT en SQL, usamos Long en Java

    @Column(nullable = false)
    private Short voto; // El CHECK de SQL asegura que sea entre 1 y 5

    @Column(name = "fecha_voto", insertable = false, updatable = false)
    private LocalDateTime fechaVoto;

    @Column(length = 300)
    private String comentario;

    // =========================================================
    // RELACIÓN FÍSICA (Con tabla de MS_Servicios)
    // =========================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cita")
    private CitaServicio cita;

    // =========================================================
    // RELACIONES LÓGICAS (Hacia MS_Usuarios)
    // =========================================================

    @Column(name = "id_trabajador", nullable = false)
    private Integer idTrabajador;

    @Column(name = "id_cliente", nullable = false)
    private Integer idCliente;
}