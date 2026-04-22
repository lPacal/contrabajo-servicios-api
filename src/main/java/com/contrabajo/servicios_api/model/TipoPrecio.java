package com.contrabajo.servicios_api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tipo_precio", schema = "dbo")
@Getter 
@Setter 
@NoArgsConstructor
public class TipoPrecio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_precio")
    private Integer id;

    @Column(nullable = false, unique = true, length = 20)
    private String nombre;
}