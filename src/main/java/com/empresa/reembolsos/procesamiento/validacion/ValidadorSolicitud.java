package com.empresa.reembolsos.procesamiento.validacion;

import com.empresa.reembolsos.dominio.SolicitudReembolso;

import java.util.List;

/**
 * Eslabon de la cadena de validacion (Chain of Responsibility, RF1).
 * <p>
 * Cada eslabon ejecuta su propia verificacion en {@link #validarPropio}; si la solicitud
 * pasa y hay un siguiente eslabon, delega en el. El primer rechazo corta la cadena. Ningun
 * eslabon sabe cuantos vienen despues: agregar una verificacion es agregar un eslabon.
 */
public abstract class ValidadorSolicitud {
    private ValidadorSolicitud siguiente;

    /**
     * Enlaza el siguiente eslabon y lo devuelve, para encadenar llamadas.
     */
    public ValidadorSolicitud siguiente(ValidadorSolicitud siguiente) {
        this.siguiente = siguiente;
        return siguiente;
    }

    /**
     * Recorre la cadena desde este eslabon y devuelve el primer rechazo, o la aprobacion
     * si todos los eslabones aprueban.
     */
    public ResultadoValidacion validar(SolicitudReembolso solicitud, List<SolicitudReembolso> pendientesDelEmpleado) {
        ResultadoValidacion propio = validarPropio(solicitud, pendientesDelEmpleado);
        if (!propio.valida() || siguiente == null) {
            return propio;
        }
        return siguiente.validar(solicitud, pendientesDelEmpleado);
    }

    /**
     * Verificacion propia del eslabon.
     *
     * @param pendientesDelEmpleado solicitudes PENDIENTE del mismo empleado, ya cargadas por
     *                              el servicio (ningun eslabon accede a la base de datos)
     */
    protected abstract ResultadoValidacion validarPropio(
        SolicitudReembolso solicitud, List<SolicitudReembolso> pendientesDelEmpleado);
}
