package com.empresa.reembolsos;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(OutputCaptureExtension.class)
class FlujoSolicitudReembolsoIntegracionTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void flujoCompleto_tresCategorias_validaCalculaYNotificaSegunElEstado(CapturedOutput salida) throws Exception {
        long viaje = crear("""
            {"empleadoId":"E10","categoria":"VIAJE","montoSolicitado":400000,"fechaGasto":"2026-10-05",
             "diasViaje":3,"documentosSoporte":true}""", "400000");
        crear("""
            {"empleadoId":"E10","categoria":"REPRESENTACION","montoSolicitado":1500000,"fechaGasto":"2026-10-08",
             "documentosSoporte":true}""", "1500000");
        long capacitacion = crear("""
            {"empleadoId":"E10","categoria":"CAPACITACION","montoSolicitado":2000000,"fechaGasto":"2026-10-10",
             "documentosSoporte":true}""", "1600000");

        cambiarEstado(viaje, "APROBADA");
        assertThat(salida.getOut())
            .contains("Notificando a E10: su solicitud paso de PENDIENTE a APROBADA")
            .contains("Contabilidad: programar pago de 400000.00 para E10")
            .contains("Auditoria: solicitud " + viaje + " cambio de PENDIENTE a APROBADA");

        cambiarEstado(capacitacion, "RECHAZADA");
        assertThat(salida.getOut())
            .contains("Auditoria: solicitud " + capacitacion + " cambio de PENDIENTE a RECHAZADA")
            .contains("Notificando a E10: su solicitud paso de PENDIENTE a RECHAZADA")
            .doesNotContain("Contabilidad: programar pago de 1600000");
    }

    @Test
    void crear_sinDocumentosSoporte_responde400ConElMotivo() throws Exception {
        mvc.perform(post("/api/solicitudes").contentType(MediaType.APPLICATION_JSON).content("""
                {"empleadoId":"E11","categoria":"VIAJE","montoSolicitado":100000,"fechaGasto":"2026-10-05",
                 "diasViaje":1,"documentosSoporte":false}"""))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Faltan documentos soporte"));
    }

    @Test
    void crear_duplicadaEnElMismoPeriodo_responde400() throws Exception {
        String json = """
            {"empleadoId":"E12","categoria":"CAPACITACION","montoSolicitado":1000,"fechaGasto":"2026-09-10",
             "documentosSoporte":true}""";
        mvc.perform(post("/api/solicitudes").contentType(MediaType.APPLICATION_JSON).content(json))
            .andExpect(status().isOk());
        mvc.perform(post("/api/solicitudes").contentType(MediaType.APPLICATION_JSON).content(json))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Ya existe una solicitud pendiente de la misma categoria en el periodo"));
    }

    @Test
    void crear_montoSobreElTopeDeLaCategoria_responde400() throws Exception {
        mvc.perform(post("/api/solicitudes").contentType(MediaType.APPLICATION_JSON).content("""
                {"empleadoId":"E13","categoria":"REPRESENTACION","montoSolicitado":2500000,"fechaGasto":"2026-10-08",
                 "documentosSoporte":true}"""))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Monto excede el tope de la categoria REPRESENTACION (2000000)"));
    }

    @Test
    void cambiarEstado_solicitudInexistente_responde404() throws Exception {
        mvc.perform(put("/api/solicitudes/9999/estado").contentType(MediaType.APPLICATION_JSON).content("\"APROBADA\""))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("Solicitud no encontrada: 9999"));
    }

    /** Crea la solicitud, verifica que quede PENDIENTE con el monto aprobado esperado y devuelve su id. */
    private long crear(String json, String montoAprobadoEsperado) throws Exception {
        String respuesta = mvc.perform(post("/api/solicitudes").contentType(MediaType.APPLICATION_JSON).content(json))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.estado").value("PENDIENTE"))
            .andReturn().getResponse().getContentAsString();
        Number montoAprobado = JsonPath.read(respuesta, "$.montoAprobado");
        assertThat(new BigDecimal(montoAprobado.toString())).isEqualByComparingTo(montoAprobadoEsperado);
        return ((Number) JsonPath.read(respuesta, "$.id")).longValue();
    }

    private void cambiarEstado(long id, String estado) throws Exception {
        mvc.perform(put("/api/solicitudes/" + id + "/estado").contentType(MediaType.APPLICATION_JSON).content("\"" + estado + "\""))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.estado").value(estado));
    }
}
