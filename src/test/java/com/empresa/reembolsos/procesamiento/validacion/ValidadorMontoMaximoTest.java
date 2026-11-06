package com.empresa.reembolsos.procesamiento.validacion;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.SolicitudReembolso;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.empresa.reembolsos.Solicitudes.valida;
import static org.assertj.core.api.Assertions.assertThat;

class ValidadorMontoMaximoTest {

    private final ValidadorMontoMaximo validador = new ValidadorMontoMaximo();

    @Test
    void validar_representacionJustoEnElTope_apruebe() {
        assertThat(validador.validar(valida(CategoriaGasto.REPRESENTACION, "2000000"), List.of()).valida()).isTrue();
    }

    @Test
    void validar_capacitacionSobreElTope_rechaza() {
        assertThat(validador.validar(valida(CategoriaGasto.CAPACITACION, "3000001"), List.of()).motivoRechazo())
            .isEqualTo("Monto excede el tope de la categoria CAPACITACION (3000000)");
    }

    @Test
    void validar_viajeDeTresDias_usaElTopeDiarioPorDias() {
        SolicitudReembolso enElTope = viaje("450000", 3);
        SolicitudReembolso sobreElTope = viaje("450001", 3);

        assertThat(validador.validar(enElTope, List.of()).valida()).isTrue();
        assertThat(validador.validar(sobreElTope, List.of()).motivoRechazo())
            .isEqualTo("Monto excede el tope de la categoria VIAJE (450000)");
    }

    @Test
    void validar_viajeSinDias_rechaza() {
        assertThat(validador.validar(viaje("1000", 0), List.of()).valida()).isFalse();
    }

    private SolicitudReembolso viaje(String monto, int dias) {
        SolicitudReembolso s = valida(CategoriaGasto.VIAJE, monto);
        s.setDiasViaje(dias);
        return s;
    }
}
