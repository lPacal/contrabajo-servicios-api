package com.contrabajo.servicios_api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sirve los ficheros físicos de la carpeta de uploads como recursos estáticos.
 *
 * GET /fotos/{nombre_archivo}  →  {fotos-dir}/{nombre_archivo}
 *
 * No se requiere autenticación para acceder a las imágenes
 * (SecurityConfig ya tiene /fotos/** en permitAll).
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.fotos-dir}")
    private String fotosDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Normaliza la ruta y garantiza la barra final
        String location = "file:" + fotosDir.replace("\\", "/");
        if (!location.endsWith("/")) location += "/";

        registry.addResourceHandler("/fotos/**")
                .addResourceLocations(location);
    }
}
