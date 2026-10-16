package cl.duoc.atlas.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.duoc.atlas.domain.exception.DatosInvalidosException;
import cl.duoc.atlas.domain.exception.RecursoNoEncontradoException;
import cl.duoc.atlas.domain.gateway.ClienteGateway;
import cl.duoc.atlas.domain.gateway.ContratoGateway;
import cl.duoc.atlas.domain.model.Cliente;
import cl.duoc.atlas.domain.model.Contrato;
import cl.duoc.atlas.domain.model.EstadoContrato;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ContratoUseCaseTest {

  private static final String USUARIO = "admin";
  private static final LocalDate HOY = LocalDate.of(2026, 10, 16);

  @Mock private ContratoGateway contratos;
  @Mock private ClienteGateway clientes;

  private ContratoUseCase useCase;

  @BeforeEach
  void setUp() {
    Clock reloj = Clock.fixed(Instant.parse("2026-10-16T15:00:00Z"), ZoneId.of("America/Santiago"));
    useCase = new ContratoUseCase(contratos, clientes, reloj);
  }

  private Cliente clienteActivo() {
    return new Cliente(1L, "12345678-5", "C", null, "d", "a@b.cl", "12345678", true);
  }

  private Contrato contrato(Long id, LocalDate inicio, LocalDate termino) {
    return new Contrato(id, 1L, "Suministro", "desc", inicio, termino);
  }

  @Test
  void listarDevuelveLosContratosDelCliente() {
    List<Contrato> lista = List.of(contrato(1L, HOY, HOY.plusDays(90)));
    when(clientes.findById(1L)).thenReturn(Optional.of(clienteActivo()));
    when(contratos.findByClienteId(1L)).thenReturn(lista);

    assertThat(useCase.listarPorCliente(1L)).isSameAs(lista);
  }

  @Test
  void listarFallaSiElClienteNoExisteOEstaEliminado() {
    Cliente eliminado = clienteActivo();
    eliminado.setActivo(false);
    when(clientes.findById(1L)).thenReturn(Optional.of(eliminado));
    when(clientes.findById(2L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> useCase.listarPorCliente(1L))
        .isInstanceOf(RecursoNoEncontradoException.class)
        .hasMessage("No existe un cliente con id 1");
    assertThatThrownBy(() -> useCase.listarPorCliente(2L))
        .isInstanceOf(RecursoNoEncontradoException.class);
    verify(contratos, never()).findByClienteId(any());
  }

  @Test
  void obtenerDevuelveElContrato() {
    Contrato c = contrato(3L, HOY, HOY);
    when(contratos.findById(3L)).thenReturn(Optional.of(c));

    assertThat(useCase.obtener(3L)).isSameAs(c);
  }

  @Test
  void obtenerFallaSiNoExiste() {
    when(contratos.findById(3L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> useCase.obtener(3L))
        .isInstanceOf(RecursoNoEncontradoException.class)
        .hasMessage("No existe un contrato con id 3");
  }

  @Test
  void crearAsociaAlClienteYDescartaElId() {
    when(clientes.findById(1L)).thenReturn(Optional.of(clienteActivo()));
    when(contratos.save(any(Contrato.class)))
        .thenAnswer(
            inv -> {
              Contrato c = inv.getArgument(0);
              c.setId(10L);
              return c;
            });
    Contrato nuevo = new Contrato(77L, 99L, "T", null, HOY, HOY.plusDays(10));

    Contrato creado = useCase.crear(1L, nuevo, USUARIO);

    ArgumentCaptor<Contrato> captor = ArgumentCaptor.forClass(Contrato.class);
    verify(contratos).save(captor.capture());
    assertThat(captor.getValue().getClienteId()).isEqualTo(1L);
    assertThat(creado.getId()).isEqualTo(10L);
  }

  @Test
  void crearNoEnviaElIdEntrante() {
    when(clientes.findById(1L)).thenReturn(Optional.of(clienteActivo()));
    when(contratos.save(any(Contrato.class))).thenAnswer(inv -> inv.getArgument(0));

    assertThat(useCase.crear(1L, contrato(77L, HOY, HOY), USUARIO).getId()).isNull();
  }

  @Test
  void crearFallaConFechasIncoherentes() {
    when(clientes.findById(1L)).thenReturn(Optional.of(clienteActivo()));

    assertThatThrownBy(() -> useCase.crear(1L, contrato(null, HOY, HOY.minusDays(1)), USUARIO))
        .isInstanceOf(DatosInvalidosException.class)
        .hasMessageContaining("fecha de término");
    verify(contratos, never()).save(any(Contrato.class));
  }

  @Test
  void crearFallaSiElClienteNoExiste() {
    when(clientes.findById(5L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> useCase.crear(5L, contrato(null, HOY, HOY), USUARIO))
        .isInstanceOf(RecursoNoEncontradoException.class);
    verify(contratos, never()).save(any(Contrato.class));
  }

  @Test
  void actualizarCopiaLosCamposPeroNoCambiaElCliente() {
    Contrato actual = contrato(3L, HOY, HOY.plusDays(5));
    Contrato cambios = new Contrato(null, 99L, "Nuevo", "nd", HOY.plusDays(1), HOY.plusDays(200));
    when(contratos.findById(3L)).thenReturn(Optional.of(actual));
    when(contratos.save(any(Contrato.class))).thenAnswer(inv -> inv.getArgument(0));

    Contrato r = useCase.actualizar(3L, cambios, USUARIO);

    assertThat(r.getId()).isEqualTo(3L);
    assertThat(r.getClienteId()).isEqualTo(1L);
    assertThat(r.getTitulo()).isEqualTo("Nuevo");
    assertThat(r.getDescripcion()).isEqualTo("nd");
    assertThat(r.getFechaInicio()).isEqualTo(HOY.plusDays(1));
    assertThat(r.getFechaTermino()).isEqualTo(HOY.plusDays(200));
    verify(contratos).save(actual);
  }

  @Test
  void actualizarFallaConFechasIncoherentes() {
    when(contratos.findById(3L)).thenReturn(Optional.of(contrato(3L, HOY, HOY)));

    assertThatThrownBy(() -> useCase.actualizar(3L, contrato(null, HOY, HOY.minusDays(2)), USUARIO))
        .isInstanceOf(DatosInvalidosException.class);
    verify(contratos, never()).save(any(Contrato.class));
  }

  @Test
  void eliminarBorraSiExiste() {
    when(contratos.findById(3L)).thenReturn(Optional.of(contrato(3L, HOY, HOY)));

    useCase.eliminar(3L, USUARIO);

    verify(contratos).deleteById(3L);
  }

  @Test
  void eliminarFallaSiNoExiste() {
    when(contratos.findById(3L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> useCase.eliminar(3L, USUARIO))
        .isInstanceOf(RecursoNoEncontradoException.class);
    verify(contratos, never()).deleteById(any());
  }

  @Test
  void estadoUsaLaFechaDelReloj() {
    assertThat(useCase.estado(contrato(1L, HOY.minusDays(100), HOY.minusDays(1))))
        .isEqualTo(EstadoContrato.EXPIRADO);
    assertThat(useCase.estado(contrato(1L, HOY.minusDays(100), HOY)))
        .isEqualTo(EstadoContrato.POR_VENCER);
    assertThat(useCase.estado(contrato(1L, HOY, HOY.plusDays(31))))
        .isEqualTo(EstadoContrato.ACTIVO);
  }
}
