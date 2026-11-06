package com.empresa.reembolsos.procesamiento.notificacion;

import com.empresa.reembolsos.dominio.EstadoSolicitud;
import com.empresa.reembolsos.dominio.SolicitudReembolso;

/**
 * Interesado en los cambios de estado de una solicitud (Observer, RF3).
 * <p>
 * El servicio de aplicacion notifica a todos los observadores registrados en Spring sin
 * conocer sus detalles; cada uno decide si reacciona y como. Un interesado nuevo (por
 * ejemplo, un panel de metricas) es una implementacion nueva.
 */
public interface ObservadorCambioEstado {

    /**
     * @param solicitud      la solicitud ya guardada con su nuevo estado
     * @param estadoAnterior el estado que tenia antes del cambio
     */
    void onCambioEstado(SolicitudReembolso solicitud, EstadoSolicitud estadoAnterior);
}
