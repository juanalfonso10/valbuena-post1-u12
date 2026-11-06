package com.empresa.reembolsos.procesamiento.validacion;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.SolicitudReembolso;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static com.empresa.reembolsos.Solicitudes.valida;
import static org.assertj.core.api.Assertions.assertThat;

class ValidadorDatosCompletosTest {

    private final ValidadorDatosCompletos validador = new ValidadorDatosCompletos();

    @Test
    void validar_solicitudCompleta_apruebe() {
        assertThat(validador.validar(valida(CategoriaGasto.VIAJE, "1000"), List.of()).valida()).isTrue();
    }

    @Test
    void validar_sinCategoriaNiFecha_rechazaNombrandoLosCampos() {
        SolicitudReembolso s = valida(CategoriaGasto.VIAJE, "1000");
        s.setCategoria(null);
        s.setFechaGasto(null);

        assertThat(validador.validar(s, List.of()).motivoRechazo())
            .isEqualTo("Faltan datos obligatorios: categoria, fechaGasto");
    }

    @Test
    void validar_montoCero_rechaza() {
        SolicitudReembolso s = valida(CategoriaGasto.VIAJE, "1000");
        s.setMontoSolicitado(BigDecimal.ZERO);

        assertThat(validador.validar(s, List.of()).motivoRechazo())
            .isEqualTo("El monto solicitado debe ser mayor que cero");
    }
}
