package com.empresa.reembolsos.procesamiento.notificacion;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.EstadoSolicitud;
import com.empresa.reembolsos.dominio.SolicitudReembolso;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.math.BigDecimal;

import static com.empresa.reembolsos.Solicitudes.valida;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(OutputCaptureExtension.class)
class NotificadoresTest {

    @Test
    void notificadorContabilidad_solicitudAprobada_programaElPago(CapturedOutput salida) {
        new NotificadorContabilidad().onCambioEstado(conEstado(EstadoSolicitud.APROBADA), EstadoSolicitud.PENDIENTE);

        assertThat(salida).contains("Contabilidad: programar pago de 1000 para E1");
    }

    @Test
    void notificadorContabilidad_solicitudRechazada_noHaceNada(CapturedOutput salida) {
        new NotificadorContabilidad().onCambioEstado(conEstado(EstadoSolicitud.RECHAZADA), EstadoSolicitud.PENDIENTE);

        assertThat(salida).doesNotContain("Contabilidad");
    }

    @Test
    void notificadorEmpleado_cualquierCambio_informaAlEmpleado(CapturedOutput salida) {
        new NotificadorEmpleado().onCambioEstado(conEstado(EstadoSolicitud.REQUIERE_CORRECCION), EstadoSolicitud.PENDIENTE);

        assertThat(salida).contains("Notificando a E1: su solicitud paso de PENDIENTE a REQUIERE_CORRECCION");
    }

    @Test
    void notificadorAuditoria_cualquierCambio_loRegistra(CapturedOutput salida) {
        new NotificadorAuditoria().onCambioEstado(conEstado(EstadoSolicitud.RECHAZADA), EstadoSolicitud.PENDIENTE);

        assertThat(salida).contains("Auditoria: solicitud 7 cambio de PENDIENTE a RECHAZADA");
    }

    private SolicitudReembolso conEstado(EstadoSolicitud estado) {
        SolicitudReembolso s = valida(CategoriaGasto.VIAJE, "1000");
        s.setId(7L);
        s.setMontoAprobado(new BigDecimal("1000"));
        s.setEstado(estado);
        return s;
    }
}
