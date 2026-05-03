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
    if (estadoRepository.count() == 0) {
        estadoRepository.save(crearEstado("CITA_PENDIENTE", "Pendiente", "El cliente generó la cita y espera revisión del trabajador."));
        estadoRepository.save(crearEstado("CITA_RECHAZADA", "Rechazada", "Trabajador rechaza propuesta."));
        estadoRepository.save(crearEstado("CITA_HANDSHAKE", "Handshake", "El trabajador aceptó condiciones iniciales."));
        estadoRepository.save(crearEstado("CITA_COMENZANDO", "Comenzando", "Trabajador solicita iniciar trabajo (espera confirmación)."));
        estadoRepository.save(crearEstado("CITA_EN_PROCESO", "En proceso", "Cliente confirma inicio del trabajo."));
        estadoRepository.save(crearEstado("CITA_FINALIZANDO", "Finalizando", "Trabajador solicita finalizar (espera confirmación)."));
        estadoRepository.save(crearEstado("CITA_FINALIZADO", "Finalizado", "Cliente confirma finalización del trabajo."));
        estadoRepository.save(crearEstado("CITA_CANCELADO", "Cancelado", "Cita cancelada por rechazo o cierre anticipado."));
    }
}

    private void poblarCategorias() {
        if (categoriaRepository.count() == 0) {
            String[] nombres = {"Electricidad", "Gasfitería", "Limpieza", "Carpintería"};
            for (String nombre : nombres) {
                CategoriaServicio cat = new CategoriaServicio();
                cat.setNombre(nombre);
                categoriaRepository.save(cat);
            }
        }
    }

    private void poblarTiposPrecio() {
        if (tipoPrecioRepository.count() == 0) {
            String[] tipos = {"Precio Fijo", "Por Hora", "A convenir"};
            for (String tipo : tipos) {
                TipoPrecio tp = new TipoPrecio();
                tp.setNombre(tipo);
                tipoPrecioRepository.save(tp);
            }
        }
    }

    private Estado crearEstado(String codigo, String nombre, String desc) {
        Estado e = new Estado();
        e.setCodigo(codigo);
        e.setNombre(nombre);
        e.setDescripcion(desc);
        return e;
    }
} // <--- ASEGÚRATE DE QUE ESTA LLAVE CIERRE LA CLASE