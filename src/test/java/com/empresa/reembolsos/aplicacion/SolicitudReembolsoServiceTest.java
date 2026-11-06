package com.empresa.reembolsos.aplicacion;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.EstadoSolicitud;
import com.empresa.reembolsos.dominio.SolicitudReembolso;
import com.empresa.reembolsos.infraestructura.RepositorioSolicitudesJpa;
import com.empresa.reembolsos.procesamiento.calculo.CalculadoraCapacitacion;
import com.empresa.reembolsos.procesamiento.calculo.SelectorCalculadora;
import com.empresa.reembolsos.procesamiento.notificacion.ObservadorCambioEstado;
import com.empresa.reembolsos.procesamiento.validacion.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static com.empresa.reembolsos.Solicitudes.valida;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SolicitudReembolsoServiceTest {

    private final RepositorioSolicitudesJpa repositorio = mock(RepositorioSolicitudesJpa.class);
    private final ObservadorCambioEstado observadorA = mock(ObservadorCambioEstado.class);
    private final ObservadorCambioEstado observadorB = mock(ObservadorCambioEstado.class);
    private SolicitudReembolsoService servicio;

    @BeforeEach
    void setUp() {
        var cadena = new CadenaValidadores(new ValidadorDatosCompletos(), new ValidadorDocumentosSoporte(),
            new ValidadorMontoMaximo(), new ValidadorDuplicados());
        servicio = new SolicitudReembolsoService(cadena, new SelectorCalculadora(List.of(new CalculadoraCapacitacion())),
            List.of(observadorA, observadorB), repositorio);
        when(repositorio.save(any())).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void crear_solicitudValida_calculaMontoYQuedaPendiente() {
        SolicitudReembolso s = valida(CategoriaGasto.CAPACITACION, "1000000");
        s.setId(99L);

        SolicitudReembolso creada = servicio.crear(s);

        assertThat(creada.getMontoAprobado()).isEqualByComparingTo("800000");
        assertThat(creada.getEstado()).isEqualTo(EstadoSolicitud.PENDIENTE);
        assertThat(creada.getId()).as("crear nunca sobrescribe una solicitud existente").isNull();
    }

    @Test
    void crear_solicitudInvalida_lanzaExcepcionYNoGuarda() {
        SolicitudReembolso s = valida(CategoriaGasto.CAPACITACION, "1000");
        s.setDocumentosSoporte(false);

        assertThatThrownBy(() -> servicio.crear(s))
            .isInstanceOf(SolicitudInvalidaException.class).hasMessage("Faltan documentos soporte");
        verify(repositorio, never()).save(any());
    }

    @Test
    void cambiarEstado_solicitudExistente_notificaATodosLosObservadoresConElEstadoAnterior() {
        SolicitudReembolso s = valida(CategoriaGasto.CAPACITACION, "1000");
        s.setEstado(EstadoSolicitud.PENDIENTE);
        when(repositorio.findById(1L)).thenReturn(Optional.of(s));

        servicio.cambiarEstado(1L, EstadoSolicitud.APROBADA);

        verify(observadorA).onCambioEstado(s, EstadoSolicitud.PENDIENTE);
        verify(observadorB).onCambioEstado(s, EstadoSolicitud.PENDIENTE);
        assertThat(s.getEstado()).isEqualTo(EstadoSolicitud.APROBADA);
    }

    @Test
    void cambiarEstado_solicitudInexistente_lanzaExcepcionYNoNotifica() {
        when(repositorio.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.cambiarEstado(5L, EstadoSolicitud.APROBADA))
            .isInstanceOf(SolicitudNoEncontradaException.class);
        verifyNoInteractions(observadorA, observadorB);
    }
}
