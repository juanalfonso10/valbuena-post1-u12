package com.empresa.reembolsos.procesamiento.validacion;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.SolicitudReembolso;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static com.empresa.reembolsos.Solicitudes.pendiente;
import static com.empresa.reembolsos.Solicitudes.valida;
import static org.assertj.core.api.Assertions.assertThat;

class CadenaValidadoresTest {

    private final CadenaValidadores cadena = new CadenaValidadores(new ValidadorDatosCompletos(),
        new ValidadorDocumentosSoporte(), new ValidadorMontoMaximo(), new ValidadorDuplicados());

    @Test
    void validar_solicitudQuePasaTodosLosEslabones_apruebe() {
        assertThat(cadena.validar(valida(CategoriaGasto.CAPACITACION, "500000"), List.of()).valida()).isTrue();
    }

    @Test
    void validar_variasFallasALaVez_reportaLaPrimeraSegunElOrden() {
        // Sin documentos, sobre el tope y duplicada: gana documentos, que va antes
        SolicitudReembolso s = valida(CategoriaGasto.REPRESENTACION, "9000000");
        s.setDocumentosSoporte(false);
        var pendientes = List.of(pendiente(CategoriaGasto.REPRESENTACION, LocalDate.of(2026, 10, 1)));

        assertThat(cadena.validar(s, pendientes).motivoRechazo()).isEqualTo("Faltan documentos soporte");
    }

    @Test
    void validar_datosIncompletos_cortaAntesDeLosValidadoresQueLosNecesitan() {
        // Sin categoria, ValidadorMontoMaximo fallaria con NullPointerException si llegara a ejecutarse
        SolicitudReembolso s = valida(CategoriaGasto.VIAJE, "1000");
        s.setCategoria(null);

        assertThat(cadena.validar(s, List.of()).motivoRechazo()).isEqualTo("Faltan datos obligatorios: categoria");
    }
}
