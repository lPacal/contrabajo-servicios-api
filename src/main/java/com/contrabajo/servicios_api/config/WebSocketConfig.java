package com.contrabajo.servicios_api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Esta es la URL a la que se conectará el Frontend (Kotlin)
        registry.addEndpoint("/ws-servicios")
                .setAllowedOriginPatterns("*") // CORS: Permite conexiones desde cualquier lugar
                .withSockJS(); // Fallback para navegadores que no soportan WebSocket nativo
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // /topic: Para mensajes globales (ej. Todos ven un servicio nuevo)
        // /queue: Para mensajes privados (ej. "¡Felicidades, tomaste el servicio!")
        config.enableSimpleBroker("/topic", "/queue");

        // Si el cliente nos quiere mandar un mensaje por WS, usa este prefijo
        config.setApplicationDestinationPrefixes("/app");
    }
}