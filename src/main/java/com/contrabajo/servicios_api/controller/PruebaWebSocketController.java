package com.contrabajo.servicios_api.controller;

import com.contrabajo.servicios_api.dto.NotificacionWebSocketDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/prueba-ws")
public class PruebaWebSocketController {

    // ESTA ES LA MAGIA: El objeto que envía mensajes por WebSocket
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @PostMapping("/enviar-global")
    public String simularNuevoServicio() {
        
        // 1. Creamos el mensaje
        // 1. Creamos el mensaje pasándole los 4 parámetros
        NotificacionWebSocketDTO notificacion = new NotificacionWebSocketDTO(
                "NUEVO_SERVICIO",
                123L,
                "¡Alguien necesita un contrabajista urgente!",
                java.time.LocalDateTime.now() // <--- Agregamos este 4to parámetro
        );

        // 2. Lo empujamos al canal global "/topic/servicios"
        messagingTemplate.convertAndSend("/topic/servicios", notificacion);

        return "Mensaje enviado por WebSocket a todos los suscriptores.";
    }
}