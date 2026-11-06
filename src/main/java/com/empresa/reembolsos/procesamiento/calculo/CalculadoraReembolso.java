package com.empresa.reembolsos.procesamiento.calculo;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.SolicitudReembolso;

import java.math.BigDecimal;

/**
 * Estrategia de calculo del monto aprobado para una categoria de gasto (Strategy, RF2).
 * <p>
 * Cada implementacion es un {@code @Component}: Spring las inyecta todas en
 * {@link SelectorCalculadora}, asi que una categoria nueva solo requiere una clase nueva.
 */
public interface CalculadoraReembolso {

    /** Categoria que resuelve esta estrategia; debe ser unica entre las implementaciones. */
    CategoriaGasto getCategoria();

    /** Monto aprobado para la solicitud segun la formula de la categoria. */
    BigDecimal calcular(SolicitudReembolso solicitud);
}
