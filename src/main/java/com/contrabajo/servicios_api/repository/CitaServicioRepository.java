package com.contrabajo.servicios_api.repository;

import com.contrabajo.servicios_api.model.CitaServicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CitaServicioRepository extends JpaRepository<CitaServicio, Integer> {
    
    // Historial de un cliente ordenado por fecha
    List<CitaServicio> findByIdClienteOrderByFechaSolicitudDesc(Integer idCliente);
    
    // Historial de un trabajador
    List<CitaServicio> findByIdTrabajadorOrderByFechaSolicitudDesc(Integer idTrabajador);
    
    // Traer todas las citas que estén en un estado particular (ej: "PENDIENTE")
    List<CitaServicio> findByEstadoIdOrderByFechaSolicitudDesc(Integer idEstado);
    
    // Traer todas las citas generadas a partir de una oferta específica
    List<CitaServicio> findByOfertaServicioId(Integer idOfertaServicio);
}