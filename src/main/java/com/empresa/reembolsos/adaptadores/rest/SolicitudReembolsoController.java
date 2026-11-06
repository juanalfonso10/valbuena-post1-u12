package com.empresa.reembolsos.adaptadores.rest;

import com.empresa.reembolsos.aplicacion.SolicitudInvalidaException;
import com.empresa.reembolsos.aplicacion.SolicitudNoEncontradaException;
import com.empresa.reembolsos.aplicacion.SolicitudReembolsoService;
import com.empresa.reembolsos.dominio.EstadoSolicitud;
import com.empresa.reembolsos.dominio.SolicitudReembolso;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// Controlador REST: solo habla con el servicio de aplicacion
@RestController
@RequestMapping("/api/solicitudes")
public class SolicitudReembolsoController {
    private final SolicitudReembolsoService servicio;

    public SolicitudReembolsoController(SolicitudReembolsoService servicio) { this.servicio = servicio; }

    @PostMapping
    public ResponseEntity<SolicitudReembolso> crear(@RequestBody SolicitudReembolso solicitud) {
        return ResponseEntity.ok(servicio.crear(solicitud));
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<SolicitudReembolso> cambiarEstado(@PathVariable Long id, @RequestBody EstadoSolicitud nuevoEstado) {
        return ResponseEntity.ok(servicio.cambiarEstado(id, nuevoEstado));
    }

    @ExceptionHandler(SolicitudInvalidaException.class)
    public ResponseEntity<Map<String, String>> solicitudInvalida(SolicitudInvalidaException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(SolicitudNoEncontradaException.class)
    public ResponseEntity<Map<String, String>> solicitudNoEncontrada(SolicitudNoEncontradaException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
    }
}
