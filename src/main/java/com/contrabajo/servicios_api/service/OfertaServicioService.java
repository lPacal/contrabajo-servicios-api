package com.contrabajo.servicios_api.service;

import com.contrabajo.servicios_api.dto.OfertaServicioCreateDTO;
import com.contrabajo.servicios_api.dto.OfertaServicioResponseDTO;
import com.contrabajo.servicios_api.dto.OfertaServicioUpdateDTO;
import com.contrabajo.servicios_api.dto.UsuarioUbicacionDTO;
import com.contrabajo.servicios_api.model.CategoriaServicio;
import com.contrabajo.servicios_api.model.OfertaServicio;
import com.contrabajo.servicios_api.model.TipoPrecio;
import com.contrabajo.servicios_api.repository.CategoriaServicioRepository;
import com.contrabajo.servicios_api.repository.OfertaServicioRepository;
import com.contrabajo.servicios_api.repository.TipoPrecioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@org.hibernate.annotations.SQLRestriction("borrado = 0")
public class OfertaServicioService {

    private final OfertaServicioRepository ofertaRepository;
    private final CategoriaServicioRepository categoriaRepository;
    private final TipoPrecioRepository tipoPrecioRepository;

    // ==========================================
    // 1. CREAR
    // ==========================================
    @Transactional
    public OfertaServicioResponseDTO crear(OfertaServicioCreateDTO dto, Integer idUsuarioAutenticado, String authorizationHeader) {
        
        OfertaServicio nuevaOferta = new OfertaServicio();
        nuevaOferta.setTitulo(dto.getTitulo());
        nuevaOferta.setDescripcion(dto.getDescripcion());
        nuevaOferta.setPrecio(dto.getPrecio());
        
        nuevaOferta.setDisponible(false);
        nuevaOferta.setBorrado(false);
        nuevaOferta.setIdTrabajador(idUsuarioAutenticado);

        if (dto.getIdCategoria() != null) {
            CategoriaServicio cat = categoriaRepository.findById(dto.getIdCategoria())
                    .orElseThrow(() -> new RuntimeException("La categoría indicada no existe."));
            nuevaOferta.setCategoriaServicio(cat);
        }

        if (dto.getIdTipoPrecio() != null) {
            TipoPrecio tp = tipoPrecioRepository.findById(dto.getIdTipoPrecio())
                    .orElseThrow(() -> new RuntimeException("El tipo de precio indicado no existe."));
            nuevaOferta.setTipoPrecio(tp);
        }

        OfertaServicio guardada = ofertaRepository.save(nuevaOferta);
        return convertirADto(guardada, authorizationHeader);
    }

    // ==========================================
    // 2. LEER
    // ==========================================
    @Transactional(readOnly = true)
    public List<OfertaServicioResponseDTO> listarTodas(String authorizationHeader) {
        return ofertaRepository.findAll().stream()
                .map(oferta -> convertirADto(oferta, authorizationHeader))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OfertaServicioResponseDTO buscarPorId(Integer id, String authorizationHeader) {
        return ofertaRepository.findById(id)
                .map(oferta -> convertirADto(oferta, authorizationHeader))
                .orElseThrow(() -> new RuntimeException("Oferta de servicio no encontrada con ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<OfertaServicioResponseDTO> listarPorTrabajador(Integer idTrabajador, String authorizationHeader) {
        return ofertaRepository.findByIdTrabajadorOrderByFechaPublicacionDesc(idTrabajador).stream()
                .map(oferta -> convertirADto(oferta, authorizationHeader))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Boolean obtenerDisponibilidad(Integer idOferta, Integer idUsuarioAutenticado) {
        OfertaServicio oferta = ofertaRepository.findById(idOferta)
                .orElseThrow(() -> new RuntimeException("Oferta de servicio no encontrada."));
        validarPropietario(oferta, idUsuarioAutenticado);
        return Boolean.TRUE.equals(oferta.getDisponible());
    }

    // ==========================================
    // 3. ACTUALIZAR
    // ==========================================
    @Transactional
    public OfertaServicioResponseDTO actualizar(Integer idOferta, OfertaServicioUpdateDTO dto, Integer idUsuarioAutenticado, String authorizationHeader) {
        
        OfertaServicio ofertaExistente = ofertaRepository.findById(idOferta)
                .orElseThrow(() -> new RuntimeException("Oferta de servicio no encontrada."));

        if (!ofertaExistente.getIdTrabajador().equals(idUsuarioAutenticado)) {
            throw new RuntimeException("Acceso denegado: No puedes editar una oferta que no te pertenece.");
        }

        if (dto.getTitulo() != null) ofertaExistente.setTitulo(dto.getTitulo());
        if (dto.getDescripcion() != null) ofertaExistente.setDescripcion(dto.getDescripcion());
        if (dto.getPrecio() != null) ofertaExistente.setPrecio(dto.getPrecio());
        if (dto.getDisponible() != null) ofertaExistente.setDisponible(dto.getDisponible());
        
        if (dto.getIdCategoria() != null) {
            CategoriaServicio cat = categoriaRepository.findById(dto.getIdCategoria())
                    .orElseThrow(() -> new RuntimeException("La categoría indicada no existe."));
            ofertaExistente.setCategoriaServicio(cat);
        }

        if (dto.getIdTipoPrecio() != null) {
            TipoPrecio tp = tipoPrecioRepository.findById(dto.getIdTipoPrecio())
                    .orElseThrow(() -> new RuntimeException("El tipo de precio indicado no existe."));
            ofertaExistente.setTipoPrecio(tp);
        }

        OfertaServicio actualizada = ofertaRepository.save(ofertaExistente);
        return convertirADto(actualizada, authorizationHeader);
    }

    @Transactional
    public OfertaServicioResponseDTO activarDisponibilidad(Integer idOferta, Integer idUsuarioAutenticado, String authorizationHeader) {
        return cambiarDisponibilidad(idOferta, idUsuarioAutenticado, true, authorizationHeader);
    }

    @Transactional
    public OfertaServicioResponseDTO desactivarDisponibilidad(Integer idOferta, Integer idUsuarioAutenticado, String authorizationHeader) {
        return cambiarDisponibilidad(idOferta, idUsuarioAutenticado, false, authorizationHeader);
    }

    // ==========================================
    // 4. ELIMINAR (Soft Delete)
    // ==========================================
    @Transactional
    public void eliminar(Integer idOferta, Integer idUsuarioAutenticado) {
        OfertaServicio ofertaExistente = ofertaRepository.findById(idOferta)
                .orElseThrow(() -> new RuntimeException("Oferta de servicio no encontrada."));

        if (!ofertaExistente.getIdTrabajador().equals(idUsuarioAutenticado)) {
            throw new RuntimeException("Acceso denegado: No puedes eliminar una oferta que no te pertenece.");
        }

        ofertaExistente.setDisponible(false);
        ofertaExistente.setBorrado(true);
        ofertaRepository.save(ofertaExistente);
    }

    private OfertaServicioResponseDTO cambiarDisponibilidad(
            Integer idOferta,
            Integer idUsuarioAutenticado,
            boolean disponible,
            String authorizationHeader
    ) {
        OfertaServicio ofertaExistente = ofertaRepository.findById(idOferta)
                .orElseThrow(() -> new RuntimeException("Oferta de servicio no encontrada."));
        validarPropietario(ofertaExistente, idUsuarioAutenticado);
        if (Boolean.TRUE.equals(ofertaExistente.getBorrado())) {
            throw new RuntimeException("No puedes cambiar la disponibilidad de una oferta eliminada.");
        }
        if (Boolean.TRUE.equals(disponible) && existeOtroServicioActivo(ofertaExistente.getIdTrabajador(), ofertaExistente.getId())) {
            throw new RuntimeException("Ya tienes un servicio activo. Desactivalo antes de activar otro.");
        }
        ofertaExistente.setDisponible(disponible);
        OfertaServicio actualizada = ofertaRepository.save(ofertaExistente);
        return convertirADto(actualizada, authorizationHeader);
    }

    private boolean existeOtroServicioActivo(Integer idTrabajador, Integer idOfertaExcluir) {
        return ofertaRepository.findByIdTrabajadorOrderByFechaPublicacionDesc(idTrabajador).stream()
                .anyMatch(oferta -> !oferta.getId().equals(idOfertaExcluir) && Boolean.TRUE.equals(oferta.getDisponible()) && !Boolean.TRUE.equals(oferta.getBorrado()));
    }

    private void validarPropietario(OfertaServicio oferta, Integer idUsuarioAutenticado) {
        if (!oferta.getIdTrabajador().equals(idUsuarioAutenticado)) {
            throw new RuntimeException("Acceso denegado: No puedes modificar una oferta que no te pertenece.");
        }
    }

    // ==========================================
    // UTILS: Mapeador
    // ==========================================
    private OfertaServicioResponseDTO convertirADto(OfertaServicio oferta, String authorizationHeader) {
        OfertaServicioResponseDTO dto = new OfertaServicioResponseDTO();
        dto.setId(oferta.getId());
        dto.setTitulo(oferta.getTitulo());
        dto.setDescripcion(oferta.getDescripcion());
        dto.setPrecio(oferta.getPrecio());
        dto.setDisponible(oferta.getDisponible());
        dto.setBorrado(oferta.getBorrado());
        dto.setFechaPublicacion(oferta.getFechaPublicacion());
        dto.setIdTrabajador(oferta.getIdTrabajador());
        
        if (oferta.getCategoriaServicio() != null) {
            dto.setIdCategoria(oferta.getCategoriaServicio().getId());
            dto.setCategoria(oferta.getCategoriaServicio().getNombre());
        }
        if (oferta.getTipoPrecio() != null) {
            dto.setIdTipoPrecio(oferta.getTipoPrecio().getId());
            dto.setTipoPrecio(oferta.getTipoPrecio().getNombre());
        }
        completarUbicacionTrabajador(dto, oferta.getIdTrabajador(), authorizationHeader);
        return dto;
    }

    private void completarUbicacionTrabajador(OfertaServicioResponseDTO dto, Integer idTrabajador, String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank() || idTrabajador == null) {
            return;
        }
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authorizationHeader);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            UsuarioUbicacionDTO usuario = restTemplate.exchange(
                    "http://localhost:8081/api/usuarios/" + idTrabajador,
                    HttpMethod.GET,
                    entity,
                    UsuarioUbicacionDTO.class
            ).getBody();
            if (usuario == null) {
                return;
            }
            String nombre = usuario.getNombre() != null ? usuario.getNombre().trim() : "";
            String apellido = usuario.getApellidoPaterno() != null ? usuario.getApellidoPaterno().trim() : "";
            dto.setNombreTrabajador((nombre + " " + apellido).trim());
            dto.setUsernameTrabajador(usuario.getUsername() != null ? usuario.getUsername() : "");
            if (usuario.getDireccion() == null) {
                return;
            }
            UsuarioUbicacionDTO.DireccionUbicacionDTO direccion = usuario.getDireccion();
            String comuna = direccion.getComuna() != null ? direccion.getComuna().getNombre() : "";
            String region = direccion.getComuna() != null && direccion.getComuna().getRegion() != null
                    ? direccion.getComuna().getRegion() : "Region Metropolitana";
            dto.setUbicacionReferencia(construirUbicacionReferencia(comuna, region));
            if (direccion.getLatitud() != null) {
                dto.setLatitudReferencia(direccion.getLatitud().doubleValue());
            }
            if (direccion.getLongitud() != null) {
                dto.setLongitudReferencia(direccion.getLongitud().doubleValue());
            }
            dto.setRangoDisponibilidadM(usuario.getRangoDisponibilidadM());
        } catch (RestClientException e) {
            // Si usuarios no responde, mantenemos la oferta visible sin radio geográfico.
        }
    }

    private String construirUbicacionReferencia(String comuna, String region) {
        String comunaLimpia = comuna == null ? "" : comuna.trim();
        String regionLimpia = region == null ? "" : region.trim();
        if (comunaLimpia.isBlank() && regionLimpia.isBlank()) {
            return "Region Metropolitana";
        }
        if (comunaLimpia.isBlank()) {
            return regionLimpia;
        }
        if (regionLimpia.isBlank()) {
            return comunaLimpia;
        }
        return comunaLimpia + ", " + regionLimpia;
    }
}
