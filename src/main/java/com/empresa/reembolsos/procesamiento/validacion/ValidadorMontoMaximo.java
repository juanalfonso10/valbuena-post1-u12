package com.empresa.reembolsos.procesamiento.validacion;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.SolicitudReembolso;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Component
public class ValidadorMontoMaximo extends ValidadorSolicitud {
    private static final BigDecimal TOPE_DIARIO_VIAJE = new BigDecimal("150000");

    // El tope de VIAJE depende de la solicitud (RF2: tope diario por dias de viaje);
    // los de REPRESENTACION y CAPACITACION son fijos.
    private static final Map<CategoriaGasto, Function<SolicitudReembolso, BigDecimal>> TOPES = Map.of(
        CategoriaGasto.VIAJE, s -> TOPE_DIARIO_VIAJE.multiply(BigDecimal.valueOf(s.getDiasViaje())),
        CategoriaGasto.REPRESENTACION, s -> new BigDecimal("2000000"),
        CategoriaGasto.CAPACITACION, s -> new BigDecimal("3000000"));

    @Override
    protected ResultadoValidacion validarPropio(SolicitudReembolso solicitud, List<SolicitudReembolso> pendientes) {
        var calculoTope = TOPES.get(solicitud.getCategoria());
        if (calculoTope == null) {
            return ResultadoValidacion.rechazar("No hay un tope configurado para la categoria " + solicitud.getCategoria());
        }
        var tope = calculoTope.apply(solicitud);
        return solicitud.getMontoSolicitado().compareTo(tope) > 0
            ? ResultadoValidacion.rechazar("Monto excede el tope de la categoria " + solicitud.getCategoria()
                + " (" + tope.toPlainString() + ")")
            : ResultadoValidacion.ok();
    }
}
