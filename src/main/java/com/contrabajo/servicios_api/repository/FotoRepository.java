package com.contrabajo.servicios_api.repository;

import com.contrabajo.servicios_api.model.Foto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FotoRepository extends JpaRepository<Foto, Integer> {

    // Trae todas las fotos de una oferta específica
    List<Foto> findByOfertaServicioId(Integer idOfertaServicio);

    // Trae todas las fotos subidas por un usuario (referencia lógica)
    List<Foto> findByIdUsuario(Integer idUsuario);
}
