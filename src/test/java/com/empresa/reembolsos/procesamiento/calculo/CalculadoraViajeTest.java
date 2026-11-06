package com.empresa.reembolsos.procesamiento.calculo;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.SolicitudReembolso;
import org.junit.jupiter.api.Test;

import static com.empresa.reembolsos.Solicitudes.valida;
import static org.assertj.core.api.Assertions.assertThat;

class CalculadoraViajeTest {

    private final CalculadoraViaje calculadora = new CalculadoraViaje();

    @Test
    void calcular_montoDentroDelTopeDiarioPorDias_reembolsaTodo() {
        assertThat(calculadora.calcular(viaje("400000", 3))).isEqualByComparingTo("400000");
    }

    @Test
    void calcular_montoSobreElTopeDiarioPorDias_reembolsaElTope() {
        assertThat(calculadora.calcular(viaje("500000", 3))).isEqualByComparingTo("450000");
    }

    private SolicitudReembolso viaje(String monto, int dias) {
        SolicitudReembolso s = valida(CategoriaGasto.VIAJE, monto);
        s.setDiasViaje(dias);
        return s;
    }
}
