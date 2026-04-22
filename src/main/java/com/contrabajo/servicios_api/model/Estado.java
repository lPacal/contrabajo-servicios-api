package com.contrabajo.servicios_api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "estado", schema = "dbo")
@Getter 
@Setter 
@NoArgsConstructor
public class Estado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estado")
    private Short id; // En SQL es SMALLINT, pero en Java Integer es más cómodo y compatible

    @Column(nullable = false, unique = true, length = 30)
    private String codigo;

    @Column(nullable = false, unique = true, length = 50)
    private String nombre;

    @Column(length = 100)
    private String descripcion;
}