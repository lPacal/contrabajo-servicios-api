package com.contrabajo.servicios_api.config;

import com.contrabajo.servicios_api.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.client.RestTemplate; // <-- NUEVO IMPORT

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request, 
            @NonNull HttpServletResponse response, 
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        final String authHeader = request.getHeader("Authorization");
        String username = null;
        String jwt = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(jwt);
            } catch (Exception e) {
                logger.error("Error al extraer username del token o firma inválida: " + e.getMessage());
            }
        }

        // Si hay usuario y aún no está autenticado en este hilo
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            
            // 1. Validamos la firma criptográfica localmente
            if (jwtUtil.validarToken(jwt, username)) {
                
                // 2. ¡NUEVO!: Preguntamos a MS_Usuarios si el token sigue activo en BD
                boolean tokenActivoEnDb = false;
                try {
                    RestTemplate restTemplate = new RestTemplate();
                    // OJO: Asegúrate de que el puerto sea el de MS_Usuarios (8081)
                    String url = "http://localhost:8081/api/auth/validar-bd?token=" + jwt; 
                    Boolean respuesta = restTemplate.getForObject(url, Boolean.class);
                    tokenActivoEnDb = Boolean.TRUE.equals(respuesta);
                } catch (Exception e) {
                    logger.error("No se pudo conectar con MS_Usuarios para validar el estado del token: " + e.getMessage());
                }

                // 3. Solo damos acceso si la matemática está bien Y la BD dice que está activo (1)
                if (tokenActivoEnDb) {
                    String rolToken = jwtUtil.extractRol(jwt);
                    
                    // Traductor de ID a Nombre de Rol (por si en tu JWT viene un "3" o "2")
                    String rolFinal;
                    if ("3".equals(rolToken)) {
                        rolFinal = "TRABAJADOR";
                    } else if ("2".equals(rolToken)) {
                        rolFinal = "CLIENTE";
                    } else if ("1".equals(rolToken)) {
                        rolFinal = "ADMINISTRADOR";
                    } else {
                        rolFinal = rolToken; // Si ya viene la palabra, la deja tal cual
                    }
                    
                    // Spring Security necesita el prefijo "ROLE_"
                    SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + rolFinal.toUpperCase());

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            username, null, Collections.singletonList(authority));
                    
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    // Si el estado es 0, no configuramos el SecurityContext, provocando un 401/403
                    logger.warn("Bloqueo de seguridad: El token es válido matemáticamente pero figura como DESACTIVADO en la BD de Usuarios.");
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}