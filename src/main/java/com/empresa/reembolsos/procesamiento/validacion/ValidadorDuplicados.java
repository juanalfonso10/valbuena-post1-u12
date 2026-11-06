package com.empresa.reembolsos.procesamiento.validacion;

import com.empresa.reembolsos.dominio.SolicitudReembolso;
import com.empresa.reembolsos.infraestructura.RepositorioSolicitudesJpa;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.util.List;

@Component
public class ValidadorDuplicados extends ValidadorSolicitud {

    // VIOLACION INTENCIONAL de la Regla 1: procesamiento accede directamente a la persistencia.
    // Un import sin uso no basta, porque desaparece al compilar y ArchUnit analiza el bytecode.
    @Autowired(required = false)
    private RepositorioSolicitudesJpa repositorio;

    @Override
    protected ResultadoValidacion validarPropio(SolicitudReembolso solicitud, List<SolicitudReembolso> pendientes) {
        // El periodo es el mes calendario completo (mes y anio): enero de 2025 y enero
        // de 2026 son periodos distintos
        YearMonth periodo = YearMonth.from(solicitud.getFechaGasto());
        boolean existeDuplicado = pendientes.stream()
            .anyMatch(p -> p.getCategoria() == solicitud.getCategoria()
                        && YearMonth.from(p.getFechaGasto()).equals(periodo));
        return existeDuplicado
            ? ResultadoValidacion.rechazar("Ya existe una solicitud pendiente de la misma categoria en el periodo")
            : ResultadoValidacion.ok();
    }
}
