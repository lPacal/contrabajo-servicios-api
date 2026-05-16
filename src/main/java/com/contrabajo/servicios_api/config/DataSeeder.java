package com.contrabajo.servicios_api.config;

import com.contrabajo.servicios_api.model.CategoriaServicio;
import com.contrabajo.servicios_api.model.TipoPrecio;
import com.contrabajo.servicios_api.repository.CategoriaServicioRepository;
import com.contrabajo.servicios_api.repository.TipoPrecioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private static final String ESTADOS_RESOURCE = "seed/codigos-estado.csv";

    private final JdbcTemplate jdbcTemplate;
    private final CategoriaServicioRepository categoriaRepository;
    private final TipoPrecioRepository tipoPrecioRepository;

    @Override
    public void run(String... args) {
        sembrarCategorias();
        sembrarTiposPrecio();

        List<EstadoSeed> estados = construirCatalogoEstados();
        if (estados.isEmpty()) {
            log.warn("No se encontro catalogo de estados para precargar.");
            return;
        }

        jdbcTemplate.execute((Connection connection) -> {
            boolean autoCommitOriginal = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try {
                try (Statement statement = connection.createStatement()) {
                    statement.execute("SET IDENTITY_INSERT dbo.estado ON");
                }

                try (PreparedStatement update = connection.prepareStatement("""
                        UPDATE dbo.estado
                        SET codigo = ?, nombre = ?, descripcion = ?
                        WHERE id_estado = ? OR codigo = ? OR nombre = ?
                        """);
                     PreparedStatement insert = connection.prepareStatement("""
                        INSERT INTO dbo.estado (id_estado, codigo, nombre, descripcion)
                        VALUES (?, ?, ?, ?)
                        """)) {

                    for (EstadoSeed estado : estados) {
                        update.setString(1, estado.codigo());
                        update.setString(2, estado.nombre());
                        update.setString(3, estado.descripcion());
                        update.setShort(4, estado.id().shortValue());
                        update.setString(5, estado.codigo());
                        update.setString(6, estado.nombre());

                        int updated = update.executeUpdate();
                        if (updated > 0) {
                            continue;
                        }

                        insert.setShort(1, estado.id().shortValue());
                        insert.setString(2, estado.codigo());
                        insert.setString(3, estado.nombre());
                        insert.setString(4, estado.descripcion());
                        insert.executeUpdate();
                    }
                }

                connection.commit();
                log.info("Estados precargados/actualizados: {}", estados.size());
            } catch (Exception ex) {
                connection.rollback();
                throw ex;
            } finally {
                try (Statement statement = connection.createStatement()) {
                    statement.execute("SET IDENTITY_INSERT dbo.estado OFF");
                } catch (Exception ex) {
                    log.debug("No fue necesario apagar IDENTITY_INSERT para dbo.estado: {}", ex.getMessage());
                } finally {
                    connection.setAutoCommit(autoCommitOriginal);
                }
            }

            return null;
        });
    }

    private void sembrarCategorias() {
        List<String> categorias = List.of(
                "Gasfitería",
                "Electricidad",
                "Pintura",
                "Carpintería",
                "Cerrajería",
                "Limpieza",
                "Jardinería",
                "Mudanzas",
                "Techumbres y filtraciones",
                "Reparaciones del hogar",
                "Instalaciones",
                "Computación y tecnología",
                "Clases particulares",
                "Cuidado de personas",
                "Cocina y catering",
                "Fotografía y video",
                "Diseño y arte",
                "Otros"
        );

        for (String nombre : categorias) {
            if (categoriaRepository.findByNombre(nombre).isEmpty()) {
                CategoriaServicio categoria = new CategoriaServicio();
                categoria.setNombre(nombre);
                categoriaRepository.save(categoria);
            }
        }
        log.info("Categorias de servicio verificadas/cargadas: {}", categorias.size());
    }

    private void sembrarTiposPrecio() {
        List<String> tipos = List.of(
                "Por hora",
                "Por trabajo",
                "Por día",
                "A convenir"
        );

        for (String nombre : tipos) {
            if (tipoPrecioRepository.findByNombre(nombre).isEmpty()) {
                TipoPrecio tipo = new TipoPrecio();
                tipo.setNombre(nombre);
                tipoPrecioRepository.save(tipo);
            }
        }
        log.info("Tipos de precio verificados/cargados: {}", tipos.size());
    }

    private List<EstadoSeed> construirCatalogoEstados() {
        Map<Integer, EstadoSeed> estados = new LinkedHashMap<>();

        cargarEstadosBaseDesdeCsv().forEach(estado -> estados.put(estado.id(), estado));

        // El front es fuente de verdad para la experiencia operativa real.
        // Se aplican overrides y extensiones sobre la base del CSV.
        List<EstadoSeed> estadosFront = List.of(
                new EstadoSeed(201, "SRV_PUBLICADO", "Servicio publicado", "Servicio visible para clientes."),
                new EstadoSeed(202, "SRV_PAUSADO", "Servicio pausado", "Servicio temporalmente inactivo."),
                new EstadoSeed(203, "SRV_OCULTO", "Servicio oculto", "Servicio no visible en exploracion."),
                new EstadoSeed(204, "SRV_RESERVADO", "Servicio reservado", "Servicio con gestion activa de cita."),
                new EstadoSeed(301, "MSG_ENVIADO", "Mensaje enviado", "Mensaje enviado por emisor."),
                new EstadoSeed(302, "MSG_ENTREGADO", "Mensaje entregado", "Mensaje recibido en el dispositivo del receptor."),
                new EstadoSeed(303, "MSG_LEIDO", "Mensaje leido", "Mensaje abierto y leido por el receptor."),
                new EstadoSeed(304, "CHAT_ABIERTO", "Chat abierto", "Chat habilitado para escritura."),
                new EstadoSeed(305, "CHAT_CERRADO", "Chat cerrado", "Chat cerrado para escritura, solo lectura."),
                new EstadoSeed(306, "CHAT_BLOQUEADO", "Chat bloqueado", "Chat temporalmente bloqueado para nuevo contacto."),
                new EstadoSeed(401, "CITA_PENDIENTE", "Cita pendiente", "El cliente genero la cita."),
                new EstadoSeed(402, "CITA_HANDSHAKE", "Handshake", "El trabajador acepto la cita y condiciones."),
                new EstadoSeed(403, "CITA_COMENZANDO", "Comenzando", "Trabajador solicita iniciar trabajo; falta confirmacion cliente."),
                new EstadoSeed(404, "CITA_EN_PROCESO", "En proceso", "Cliente confirma inicio de trabajo."),
                new EstadoSeed(405, "CITA_FINALIZANDO", "Finalizando", "Trabajador solicita finalizar; falta confirmacion cliente."),
                new EstadoSeed(406, "CITA_FINALIZADO", "Finalizado", "Cliente confirma finalizacion del trabajo."),
                new EstadoSeed(407, "CITA_CANCELADO", "Cancelado", "Cita cancelada por rechazo o cierre anticipado."),
                new EstadoSeed(408, "CITA_CERRADO", "Cerrado", "Cita o chat cerrados para escritura."),
                new EstadoSeed(409, "CITA_RECHAZADA", "Rechazada", "Trabajador rechaza propuesta y la cita queda disponible para renegociacion.")
        );

        estadosFront.forEach(estado -> estados.put(estado.id(), estado));
        return new ArrayList<>(estados.values());
    }

    private List<EstadoSeed> cargarEstadosBaseDesdeCsv() {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new ClassPathResource(ESTADOS_RESOURCE).getInputStream(), StandardCharsets.UTF_8))) {

            List<EstadoSeed> estados = new ArrayList<>();
            String line;
            boolean header = true;

            while ((line = reader.readLine()) != null) {
                if (header) {
                    header = false;
                    continue;
                }

                if (line.isBlank()) {
                    continue;
                }

                String[] partes = line.split(";", -1);
                if (partes.length < 4) {
                    continue;
                }

                estados.add(new EstadoSeed(
                        Integer.parseInt(partes[0].trim()),
                        partes[1].trim(),
                        partes[3].trim(),
                        partes.length > 4 ? partes[4].trim() : ""
                ));
            }

            return estados;
        } catch (Exception ex) {
            log.warn("No fue posible leer el catalogo base de estados desde {}: {}", ESTADOS_RESOURCE, ex.getMessage());
            return List.of();
        }
    }

    private record EstadoSeed(Integer id, String codigo, String nombre, String descripcion) {
    }
}
