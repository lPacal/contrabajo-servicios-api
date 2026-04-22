package com.contrabajo.servicios_api.service;

import com.contrabajo.servicios_api.model.OfertaServicio;
import com.contrabajo.servicios_api.repository.OfertaServicioRepository;
import com.contrabajo.servicios_api.repository.CategoriaServicioRepository;
import com.contrabajo.servicios_api.repository.TipoPrecioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@org.hibernate.annotations.SQLRestriction("borrado = 0")
public class OfertaServicioService {

    private final OfertaServicioRepository ofertaRepository;
    private final CategoriaServicioRepository categoriaRepository;
    private final TipoPrecioRepository tipoPrecioRepository;

    // ==========================================
    // 1. CREAR (Con validación de Rol)
    // ==========================================
    @Transactional
    public OfertaServicio crear(OfertaServicio oferta, Integer idUsuarioAutenticado, String rolUsuario) {
        
        System.out.println("DEBUG - Rol recibido en el Service: [" + rolUsuario + "]");

        // Regla de Negocio: Solo los Trabajadores pueden crear ofertas
        // (Ajusta el string "TRABAJADOR" según cómo lo guardes en tu JWT o BD)
        if (rolUsuario == null || !rolUsuario.equalsIgnoreCase("TRABAJADOR")) {
            throw new RuntimeException("Acceso denegado: Solo los usuarios con perfil TRABAJADOR pueden crear servicios.");
        }

        // Validar que la categoría exista (Integridad de datos)
        if (oferta.getCategoriaServicio() == null || oferta.getCategoriaServicio().getId() == null) {
            throw new RuntimeException("La categoría del servicio es obligatoria.");
        }
        categoriaRepository.findById(oferta.getCategoriaServicio().getId())
                .orElseThrow(() -> new RuntimeException("La categoría indicada no existe."));

        // Asignamos automáticamente al creador como el trabajador de esta oferta
        oferta.setIdTrabajador(idUsuarioAutenticado);
        
        // Aseguramos que los campos protegidos no vengan manipulados
        oferta.setIdCliente(null); // Un cliente se asigna cuando se crea una "Cita", no en la oferta
        
        // Nota: fechaPublicacion no se setea aquí porque en la entidad tiene insertable = false (lo hace SQL Server)

        return ofertaRepository.save(oferta);
    }

    // ==========================================
    // 2. LEER (Read)
    // ==========================================
    @Transactional(readOnly = true)
    public List<OfertaServicio> listarTodas() {
        return ofertaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public OfertaServicio buscarPorId(Integer id) {
        return ofertaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Oferta de servicio no encontrada con ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<OfertaServicio> listarPorTrabajador(Integer idTrabajador) {
        return ofertaRepository.findByIdTrabajadorOrderByFechaPublicacionDesc(idTrabajador);
    }

    // ==========================================
    // 3. ACTUALIZAR (Restringido)
    // ==========================================
    @Transactional
    public OfertaServicio actualizar(Integer idOferta, OfertaServicio datosNuevos, Integer idUsuarioAutenticado) {
        
        // 1. Buscar la oferta existente
        OfertaServicio ofertaExistente = ofertaRepository.findById(idOferta)
                .orElseThrow(() -> new RuntimeException("Oferta de servicio no encontrada."));

        // 2. Seguridad: Validar que quien intenta editar sea el dueño de la oferta
        if (!ofertaExistente.getIdTrabajador().equals(idUsuarioAutenticado)) {
            throw new RuntimeException("Acceso denegado: No puedes editar una oferta que no te pertenece.");
        }

        // 3. Actualizar SOLO los campos permitidos
        if (datosNuevos.getTitulo() != null) {
            ofertaExistente.setTitulo(datosNuevos.getTitulo());
        }
        if (datosNuevos.getDescripcion() != null) {
            ofertaExistente.setDescripcion(datosNuevos.getDescripcion());
        }
        if (datosNuevos.getPrecio() != null) {
            ofertaExistente.setPrecio(datosNuevos.getPrecio());
        }
        if (datosNuevos.getDisponible() != null) {
            ofertaExistente.setDisponible(datosNuevos.getDisponible());
        }
        
        // Actualizar relaciones si vienen en el payload
        if (datosNuevos.getCategoriaServicio() != null && datosNuevos.getCategoriaServicio().getId() != null) {
            ofertaExistente.setCategoriaServicio(datosNuevos.getCategoriaServicio());
        }
        if (datosNuevos.getTipoPrecio() != null && datosNuevos.getTipoPrecio().getId() != null) {
            ofertaExistente.setTipoPrecio(datosNuevos.getTipoPrecio());
        }

        // IMPORTANTE: NO tocamos id, idTrabajador, idCliente ni fechaPublicacion.
        // Al usar el objeto 'ofertaExistente' que JPA ya tiene rastreado, esos datos se mantienen intactos.

        return ofertaRepository.save(ofertaExistente);
    }

    // ==========================================
    // 4. ELIMINAR (Delete / Soft Delete)
    // ==========================================
    @Transactional
    public void eliminar(Integer idOferta, Integer idUsuarioAutenticado) {
        OfertaServicio ofertaExistente = ofertaRepository.findById(idOferta)
                .orElseThrow(() -> new RuntimeException("Oferta de servicio no encontrada."));

        if (!ofertaExistente.getIdTrabajador().equals(idUsuarioAutenticado)) {
            throw new RuntimeException("Acceso denegado: No puedes eliminar una oferta que no te pertenece.");
        }

        ofertaExistente.setBorrado(true); // Marcar como borrada
        ofertaRepository.save(ofertaExistente);

    }
}