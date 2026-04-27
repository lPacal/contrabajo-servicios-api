package com.contrabajo.servicios_api.repository;

import com.contrabajo.servicios_api.model.CitaServicio;
import com.contrabajo.servicios_api.model.Valoracion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ValoracionRepository extends JpaRepository<Valoracion, Long> {
    
    List<Valoracion> findByIdTrabajadorOrderByFechaVotoDesc(Integer idTrabajador);
    
    List<Valoracion> findByIdClienteOrderByFechaVotoDesc(Integer idCliente);

    boolean existsByCita(CitaServicio cita);
}