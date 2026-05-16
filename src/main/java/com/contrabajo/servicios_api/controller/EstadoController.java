package com.contrabajo.servicios_api.controller;

import com.contrabajo.servicios_api.dto.EstadoDTO;
import com.contrabajo.servicios_api.service.EstadoCatalogoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/estados")
@RequiredArgsConstructor
public class EstadoController {

    private final EstadoCatalogoService estadoCatalogoService;

    @GetMapping
    public ResponseEntity<List<EstadoDTO>> listarEstados() {
        return ResponseEntity.ok(estadoCatalogoService.listarEstados());
    }
}
