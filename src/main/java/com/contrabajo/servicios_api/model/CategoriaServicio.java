package com.contrabajo.servicios_api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "categoria_servicio", schema = "dbo")
@Getter 
@Setter 
@NoArgsConstructor
public class CategoriaServicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cat_servicio")
    private Integer id;

    @Column(nullable = false, unique = true, length = 60)
    private String nombre;
}