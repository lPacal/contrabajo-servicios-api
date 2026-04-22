package com.contrabajo.servicios_api.repository;

import com.contrabajo.servicios_api.model.Valoracion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ValoracionRepository extends JpaRepository<Valoracion, Long> {
    
    // Trae las reseñas que le han dejado a un trabajador, de la más reciente a la más antigua
    List<Valoracion> findByIdTrabajadorOrderByFechaVotoDesc(Integer idTrabajador);
    
    // Trae las reseñas que ha escrito un cliente específico
    List<Valoracion> findByIdClienteOrderByFechaVotoDesc(Integer idCliente);
}