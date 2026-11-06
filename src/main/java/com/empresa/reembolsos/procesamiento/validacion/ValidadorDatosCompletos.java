package com.empresa.reembolsos.procesamiento.validacion;

import com.empresa.reembolsos.dominio.SolicitudReembolso;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ValidadorDatosCompletos extends ValidadorSolicitud {
    @Override
    protected ResultadoValidacion validarPropio(SolicitudReembolso solicitud, List<SolicitudReembolso> pendientes) {
        List<String> faltantes = new ArrayList<>();
        if (solicitud.getEmpleadoId() == null || solicitud.getEmpleadoId().isBlank()) {
            faltantes.add("empleadoId");
        }
        if (solicitud.getCategoria() == null) {
            faltantes.add("categoria");
        }
        if (solicitud.getMontoSolicitado() == null) {
            faltantes.add("montoSolicitado");
        }
        if (solicitud.getFechaGasto() == null) {
            faltantes.add("fechaGasto");
        }
        if (!faltantes.isEmpty()) {
            return ResultadoValidacion.rechazar("Faltan datos obligatorios: " + String.join(", ", faltantes));
        }
        return solicitud.getMontoSolicitado().signum() > 0
            ? ResultadoValidacion.ok()
            : ResultadoValidacion.rechazar("El monto solicitado debe ser mayor que cero");
    }
}
