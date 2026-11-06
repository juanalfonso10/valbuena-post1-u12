package com.empresa.reembolsos.procesamiento.validacion;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.SolicitudReembolso;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.empresa.reembolsos.Solicitudes.valida;
import static org.assertj.core.api.Assertions.assertThat;

class ValidadorDocumentosSoporteTest {

    private final ValidadorDocumentosSoporte validador = new ValidadorDocumentosSoporte();

    @Test
    void validar_conDocumentos_apruebe() {
        assertThat(validador.validar(valida(CategoriaGasto.REPRESENTACION, "1000"), List.of()).valida()).isTrue();
    }

    @Test
    void validar_sinDocumentos_rechazaConMensajeClaro() {
        SolicitudReembolso s = valida(CategoriaGasto.REPRESENTACION, "1000");
        s.setDocumentosSoporte(false);

        ResultadoValidacion r = validador.validar(s, List.of());

        assertThat(r.valida()).isFalse();
        assertThat(r.motivoRechazo()).isEqualTo("Faltan documentos soporte");
    }
}
