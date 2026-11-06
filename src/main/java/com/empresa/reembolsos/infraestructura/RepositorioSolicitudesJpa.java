package com.empresa.reembolsos.infraestructura;

import com.empresa.reembolsos.dominio.EstadoSolicitud;
import com.empresa.reembolsos.dominio.SolicitudReembolso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositorio Spring Data usado directamente por el servicio, sin puerto intermedio
 * (ver docs/adr/ADR-001.md). El mapeo de la entidad esta en META-INF/orm.xml.
 */
public interface RepositorioSolicitudesJpa extends JpaRepository<SolicitudReembolso, Long> {

    List<SolicitudReembolso> findByEmpleadoIdAndEstado(String empleadoId, EstadoSolicitud estado);
}
