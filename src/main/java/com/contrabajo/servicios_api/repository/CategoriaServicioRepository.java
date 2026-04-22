package com.contrabajo.servicios_api.repository;

import com.contrabajo.servicios_api.model.CategoriaServicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoriaServicioRepository extends JpaRepository<CategoriaServicio, Integer> {
    Optional<CategoriaServicio> findByNombre(String nombre);
}