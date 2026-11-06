package com.empresa.reembolsos;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.EstadoSolicitud;
import com.empresa.reembolsos.dominio.SolicitudReembolso;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Fabrica de solicitudes validas para las pruebas; cada prueba cambia solo lo que le importa. */
public final class Solicitudes {

    private Solicitudes() {
    }

    public static SolicitudReembolso valida(CategoriaGasto categoria, String monto) {
        SolicitudReembolso s = new SolicitudReembolso();
        s.setEmpleadoId("E1");
        s.setCategoria(categoria);
        s.setMontoSolicitado(new BigDecimal(monto));
        s.setFechaGasto(LocalDate.of(2026, 10, 15));
        s.setDiasViaje(categoria == CategoriaGasto.VIAJE ? 1 : 0);
        s.setDocumentosSoporte(true);
        return s;
    }

    public static SolicitudReembolso pendiente(CategoriaGasto categoria, LocalDate fechaGasto) {
        SolicitudReembolso s = valida(categoria, "1000");
        s.setFechaGasto(fechaGasto);
        s.setEstado(EstadoSolicitud.PENDIENTE);
        return s;
    }
}
