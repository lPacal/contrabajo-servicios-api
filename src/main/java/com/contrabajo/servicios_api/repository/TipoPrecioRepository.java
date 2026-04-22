package com.contrabajo.servicios_api.repository;

import com.contrabajo.servicios_api.model.TipoPrecio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TipoPrecioRepository extends JpaRepository<TipoPrecio, Integer> {
    Optional<TipoPrecio> findByNombre(String nombre);
}