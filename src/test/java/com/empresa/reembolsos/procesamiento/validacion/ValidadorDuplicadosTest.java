package com.empresa.reembolsos.procesamiento.validacion;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.SolicitudReembolso;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static com.empresa.reembolsos.Solicitudes.pendiente;
import static com.empresa.reembolsos.Solicitudes.valida;
import static org.assertj.core.api.Assertions.assertThat;

class ValidadorDuplicadosTest {

    private final ValidadorDuplicados validador = new ValidadorDuplicados();
    private final SolicitudReembolso nueva = valida(CategoriaGasto.VIAJE, "1000"); // octubre de 2026

    @Test
    void validar_pendienteMismaCategoriaYMismoMes_rechaza() {
        var pendientes = List.of(pendiente(CategoriaGasto.VIAJE, LocalDate.of(2026, 10, 2)));

        assertThat(validador.validar(nueva, pendientes).motivoRechazo())
            .isEqualTo("Ya existe una solicitud pendiente de la misma categoria en el periodo");
    }

    @Test
    void validar_pendienteDelMismoMesDeOtroAnio_apruebe() {
        var pendientes = List.of(pendiente(CategoriaGasto.VIAJE, LocalDate.of(2025, 10, 15)));

        assertThat(validador.validar(nueva, pendientes).valida()).isTrue();
    }

    @Test
    void validar_pendienteDeOtraCategoriaEnElMismoMes_apruebe() {
        var pendientes = List.of(pendiente(CategoriaGasto.CAPACITACION, LocalDate.of(2026, 10, 15)));

        assertThat(validador.validar(nueva, pendientes).valida()).isTrue();
    }
}
