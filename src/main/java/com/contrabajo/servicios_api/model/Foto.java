package com.contrabajo.servicios_api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "foto", schema = "dbo")
@Getter
@Setter
@NoArgsConstructor
public class Foto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_foto")
    private Integer id;

    @Column(name = "fecha_subida", insertable = false, updatable = false)
    private LocalDateTime fechaSubida;

    // =========================================================
    // Metadatos del fichero
    // =========================================================

    /** Nombre original enviado por el cliente (p.ej. "foto_cocina.jpg"). */
    @Column(name = "nombre_original", nullable = false, length = 255)
    private String nombreOriginal;

    /**
     * Nombre único en disco generado por el servidor (UUID + extensión).
     * P.ej. "a1b2c3d4-e5f6-7890-abcd-ef1234567890.jpg"
     */
    @Column(name = "nombre_archivo", nullable = false, unique = true, length = 100)
    private String nombreArchivo;

    /**
     * Ruta relativa pública para acceder a la imagen.
     * P.ej. "/fotos/a1b2c3d4-....jpg"
     * A futuro apuntará a un bucket/CDN externo.
     */
    @Column(name = "enlace", nullable = false, length = 300)
    private String enlace;

    /** Tipo MIME del fichero. Siempre empieza por "image/". */
    @Column(name = "tipo_mime", nullable = false, length = 50)
    private String tipoMime;

    /** Peso del fichero en bytes. */
    @Column(name = "tamano_bytes", nullable = false)
    private Long tamanoBytes;

    /** Ancho en píxeles (opcional; se completa si el servidor puede leer las dimensiones). */
    @Column(name = "ancho_px")
    private Integer anchoPx;

    /** Alto en píxeles (opcional). */
    @Column(name = "alto_px")
    private Integer altoPx;

    // =========================================================
    // RELACIÓN FÍSICA (misma base de datos: MS_Servicios)
    // =========================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_oferta_servicio", nullable = false)
    private OfertaServicio ofertaServicio;

    // =========================================================
    // RELACIÓN LÓGICA (hacia MS_Usuarios.dbo.usuario)
    // No se declara @ManyToOne para mantener el desacoplamiento
    // entre microservicios.
    // =========================================================

    @Column(name = "id_usuario", nullable = false)
    private Integer idUsuario;
}
