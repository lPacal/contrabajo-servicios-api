package com.contrabajo.servicios_api.service;

import com.contrabajo.servicios_api.dto.FotoResponseDTO;
import com.contrabajo.servicios_api.model.Foto;
import com.contrabajo.servicios_api.model.OfertaServicio;
import com.contrabajo.servicios_api.repository.FotoRepository;
import com.contrabajo.servicios_api.repository.OfertaServicioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FotoService {

    private static final List<String> TIPOS_PERMITIDOS =
            Arrays.asList("image/jpeg", "image/png", "image/webp");

    @Value("${app.upload.fotos-dir}")
    private String fotosDir;

    @Value("${app.upload.max-tamano-bytes}")
    private long maxTamanoBytes;

    private final FotoRepository fotoRepository;
    private final OfertaServicioRepository ofertaRepository;

    public FotoService(FotoRepository fotoRepository,
                       OfertaServicioRepository ofertaRepository) {
        this.fotoRepository = fotoRepository;
        this.ofertaRepository = ofertaRepository;
    }

    // ── Subir foto ────────────────────────────────────────────────────────────
    public FotoResponseDTO subir(MultipartFile archivo, Integer idOferta, Integer idUsuario) {

        // 1. Validar MIME
        String tipoMime = archivo.getContentType();
        if (tipoMime == null || !TIPOS_PERMITIDOS.contains(tipoMime)) {
            throw new RuntimeException("Tipo de archivo no permitido. Usa JPEG, PNG o WebP.");
        }

        // 2. Validar tamaño
        if (archivo.getSize() > maxTamanoBytes) {
            throw new RuntimeException("El archivo supera el tamaño máximo de 5 MB.");
        }

        // 3. Validar que la oferta existe y pertenece al usuario
        OfertaServicio oferta = ofertaRepository.findById(idOferta)
                .orElseThrow(() -> new RuntimeException("Oferta no encontrada."));
        if (!oferta.getIdTrabajador().equals(idUsuario)) {
            throw new RuntimeException("No tienes permiso para subir fotos a esta oferta.");
        }

        // 4. Generar nombre único en disco
        String ext = obtenerExtension(archivo.getOriginalFilename(), tipoMime);
        String nombreArchivo = UUID.randomUUID().toString() + ext;

        // 5. Guardar en disco
        try {
            Path dir = Paths.get(fotosDir);
            Files.createDirectories(dir);
            Path destino = dir.resolve(nombreArchivo);
            Files.copy(archivo.getInputStream(), destino);

            // 6. Leer dimensiones (opcional — no falla si no se puede)
            Integer anchoPx = null;
            Integer altoPx  = null;
            try {
                BufferedImage img = ImageIO.read(destino.toFile());
                if (img != null) {
                    anchoPx = img.getWidth();
                    altoPx  = img.getHeight();
                }
            } catch (Exception ignored) { /* WebP puede no soportarse nativamente */ }

            // 7. Persistir metadatos
            Foto foto = new Foto();
            foto.setNombreOriginal(archivo.getOriginalFilename() != null
                    ? archivo.getOriginalFilename() : nombreArchivo);
            foto.setNombreArchivo(nombreArchivo);
            foto.setEnlace("/fotos/" + nombreArchivo);
            foto.setTipoMime(tipoMime);
            foto.setTamanoBytes(archivo.getSize());
            foto.setAnchoPx(anchoPx);
            foto.setAltoPx(altoPx);
            foto.setOfertaServicio(oferta);
            foto.setIdUsuario(idUsuario);

            return toDTO(fotoRepository.save(foto));

        } catch (IOException e) {
            throw new RuntimeException("Error al guardar el archivo: " + e.getMessage());
        }
    }

    // ── Listar fotos de una oferta ────────────────────────────────────────────
    public List<FotoResponseDTO> listarPorOferta(Integer idOferta) {
        return fotoRepository.findByOfertaServicioId(idOferta)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // ── Eliminar foto ─────────────────────────────────────────────────────────
    public void eliminar(Integer idFoto, Integer idUsuario) {
        Foto foto = fotoRepository.findById(idFoto)
                .orElseThrow(() -> new RuntimeException("Foto no encontrada."));

        // Solo el propietario puede eliminarla
        if (!foto.getIdUsuario().equals(idUsuario)) {
            throw new RuntimeException("No tienes permiso para eliminar esta foto.");
        }

        // Borrar fichero físico
        try {
            Path fichero = Paths.get(fotosDir).resolve(foto.getNombreArchivo());
            Files.deleteIfExists(fichero);
        } catch (IOException e) {
            // Log y continúa: si el fichero ya no está, igual borramos el registro
        }

        fotoRepository.delete(foto);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private String obtenerExtension(String nombreOriginal, String tipoMime) {
        if (nombreOriginal != null && nombreOriginal.contains(".")) {
            return nombreOriginal.substring(nombreOriginal.lastIndexOf('.'));
        }
        return switch (tipoMime) {
            case "image/png"  -> ".png";
            case "image/webp" -> ".webp";
            default           -> ".jpg";
        };
    }

    private FotoResponseDTO toDTO(Foto f) {
        return new FotoResponseDTO(
                f.getId(),
                f.getEnlace(),
                f.getNombreOriginal(),
                f.getTipoMime(),
                f.getTamanoBytes(),
                f.getAnchoPx(),
                f.getAltoPx(),
                f.getFechaSubida(),
                f.getOfertaServicio().getId(),
                f.getIdUsuario()
        );
    }
}
