package com.contrabajo.servicios_api.repository;

import com.contrabajo.servicios_api.model.OfertaServicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OfertaServicioRepository extends JpaRepository<OfertaServicio, Integer> {
    
    // Trae todas las ofertas de un trabajador específico, de la más nueva a la más antigua
    List<OfertaServicio> findByIdTrabajadorOrderByFechaPublicacionDesc(Integer idTrabajador);
    
    // Filtros por relaciones físicas
    List<OfertaServicio> findByCategoriaServicioId(Integer idCategoriaServicio);
    List<OfertaServicio> findByTipoPrecioId(Integer idTipoPrecio);
}