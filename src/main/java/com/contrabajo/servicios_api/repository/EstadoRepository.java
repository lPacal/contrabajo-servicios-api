package com.contrabajo.servicios_api.repository;

import com.contrabajo.servicios_api.model.Estado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EstadoRepository extends JpaRepository<Estado, Integer> {
    // Útiles para buscar el estado exacto sin saber su ID
    Optional<Estado> findByCodigo(String codigo);
    Optional<Estado> findByNombre(String nombre);
}