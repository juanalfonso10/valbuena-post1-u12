package com.empresa.reembolsos.procesamiento.calculo;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import org.junit.jupiter.api.Test;

import static com.empresa.reembolsos.Solicitudes.valida;
import static org.assertj.core.api.Assertions.assertThat;

class CalculadoraRepresentacionTest {

    private final CalculadoraRepresentacion calculadora = new CalculadoraRepresentacion();

    @Test
    void calcular_montoBajoElTopeMensual_reembolsaElMontoCompleto() {
        assertThat(calculadora.calcular(valida(CategoriaGasto.REPRESENTACION, "1500000"))).isEqualByComparingTo("1500000");
    }

    @Test
    void calcular_montoSobreElTopeMensual_reembolsaElTope() {
        assertThat(calculadora.calcular(valida(CategoriaGasto.REPRESENTACION, "2500000"))).isEqualByComparingTo("2000000");
    }
}
