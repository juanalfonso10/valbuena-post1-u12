package com.empresa.reembolsos.procesamiento.validacion;

import com.empresa.reembolsos.dominio.SolicitudReembolso;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Arma la cadena en orden de costo creciente: primero las verificaciones que solo miran la
 * propia solicitud y al final la que recorre las solicitudes pendientes del empleado
 * (Decision de diseno 1).
 */
@Component
public class CadenaValidadores {
    private final ValidadorSolicitud primero;

    public CadenaValidadores(ValidadorDatosCompletos datosCompletos,
                              ValidadorDocumentosSoporte documentos,
                              ValidadorMontoMaximo monto,
                              ValidadorDuplicados duplicados) {
        datosCompletos.siguiente(documentos).siguiente(monto).siguiente(duplicados);
        this.primero = datosCompletos;
    }

    public ResultadoValidacion validar(SolicitudReembolso solicitud, List<SolicitudReembolso> pendientes) {
        return primero.validar(solicitud, pendientes);
    }
}
