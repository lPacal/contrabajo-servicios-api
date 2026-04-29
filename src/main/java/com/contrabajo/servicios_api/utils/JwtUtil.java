package com.contrabajo.servicios_api.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.function.Function;

@Component
public class JwtUtil {

    // 1. Inyectamos la clave secreta desde application.properties
    // (Asegúrate de tener jwt.secret definido en tu properties)
    @Value("${jwt.secret}")
    private String secretKey;

    // 2. Método para generar la llave criptográfica a partir del secreto
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // 3. Método base para desencriptar TODO el token (el "escáner")
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey()) // Usa la llave correcta
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // 4. Método genérico para extraer un dato específico
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // ==========================================
    // MÉTODOS PÚBLICOS PARA EL CONTROLLER/FILTER
    // ==========================================

    // Extraer el Username (Sujeto principal)
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Extraer el ID del usuario
    public Integer extractId(String token) {
        return extractClaim(token, claims -> claims.get("id", Integer.class));
    }

    // Extraer el Rol
    public String extractRol(String token) {
        return extractClaim(token, claims -> claims.get("rol", String.class));
    }

    public Integer extraerIdCoordenadas(String token) {
        return extractClaim(token, claims -> claims.get("idCoordenadas", Integer.class));
    }

    // Validar que el token pertenezca al usuario (opcional, útil para filtros)
    public boolean validarToken(String token, String username) {
        final String tokenUsername = extractUsername(token);
        return (tokenUsername.equals(username));
    }
}