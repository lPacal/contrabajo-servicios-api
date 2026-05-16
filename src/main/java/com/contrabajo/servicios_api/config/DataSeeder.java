package com.contrabajo.servicios_api.config;

import com.contrabajo.servicios_api.model.CategoriaServicio;
import com.contrabajo.servicios_api.model.Estado;
import com.contrabajo.servicios_api.model.TipoPrecio;
import com.contrabajo.servicios_api.repository.CategoriaServicioRepository;
import com.contrabajo.servicios_api.repository.EstadoRepository;
import com.contrabajo.servicios_api.repository.TipoPrecioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final EstadoRepository estadoRepository;
    private final CategoriaServicioRepository categoriaRepository;
    private final TipoPrecioRepository tipoPrecioRepository;

    // Constructor manual (Para evitar errores de Lombok)
    public DataSeeder(EstadoRepository estadoRepository, 
                      CategoriaServicioRepository categoriaRepository, 
                      TipoPrecioRepository tipoPrecioRepository) {
        this.estadoRepository = estadoRepository;
        this.categoriaRepository = categoriaRepository;
        this.tipoPrecioRepository = tipoPrecioRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Iniciando el sembrado de datos...");
        poblarEstados();
        poblarCategorias();
        poblarTiposPrecio();
        System.out.println("Base de Datos de Servicios poblada exitosamente.");
    }

    private void poblarEstados() {
        asegurarEstado("PEND", "Pendiente", "Servicio solicitado.");
        asegurarEstado("ACEP", "Aceptado", "Trabajador aceptó.");
        asegurarEstado("FINA", "Finalizado", "Trabajo completado.");
        asegurarEstado("CANC", "Cancelado", "Servicio anulado.");
    }

    private void poblarCategorias() {
        String[] nombres = {"Electricidad", "Gasfitería", "Limpieza", "Carpintería"};
        for (String nombre : nombres) {
            if (categoriaRepository.findByNombre(nombre).isEmpty()) {
                CategoriaServicio cat = new CategoriaServicio();
                cat.setNombre(nombre);
                categoriaRepository.save(cat);
            }
        }
    }

    private void poblarTiposPrecio() {
        String[] tipos = {"Precio Fijo", "Por Hora", "A convenir"};
        for (String tipo : tipos) {
            if (tipoPrecioRepository.findByNombre(tipo).isEmpty()) {
                TipoPrecio tp = new TipoPrecio();
                tp.setNombre(tipo);
                tipoPrecioRepository.save(tp);
            }
        }
    }

    private void asegurarEstado(String codigo, String nombre, String desc) {
        Estado estado = estadoRepository.findByCodigo(codigo)
                .or(() -> estadoRepository.findByNombre(nombre))
                .orElseGet(Estado::new);
        estado.setCodigo(codigo);
        estado.setNombre(nombre);
        estado.setDescripcion(desc);
        estadoRepository.save(estado);
    }
} // <--- ASEGÚRATE DE QUE ESTA LLAVE CIERRE LA CLASE
