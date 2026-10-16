package cl.duoc.atlas.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.duoc.atlas.domain.exception.DatosInvalidosException;
import cl.duoc.atlas.domain.exception.RecursoDuplicadoException;
import cl.duoc.atlas.domain.exception.RecursoNoEncontradoException;
import cl.duoc.atlas.domain.gateway.ClienteGateway;
import cl.duoc.atlas.domain.model.Cliente;
import cl.duoc.atlas.domain.model.Pagina;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ClienteUseCaseTest {

  private static final String USUARIO = "admin";

  @Mock private ClienteGateway gateway;

  @InjectMocks private ClienteUseCase useCase;

  private Cliente cliente(Long id, String rut) {
    return new Cliente(id, rut, "Ferretería", "desc", "Av. 1", "a@b.cl", "+56911111111", true);
  }

  @Test
  void listarUsaElFiltroRecortadoYLaPaginacion() {
    Pagina<Cliente> pagina = new Pagina<>(List.of(cliente(1L, "12345678-5")), 1, 5, 6);
    when(gateway.buscarActivos("ferre", 1, 5)).thenReturn(pagina);

    assertThat(useCase.listar("  ferre ", 1, 5)).isSameAs(pagina);
  }

  @Test
  void listarConNombreNuloBuscaTodos() {
    Pagina<Cliente> vacia = new Pagina<>(List.of(), 0, 10, 0);
    when(gateway.buscarActivos("", 0, 10)).thenReturn(vacia);

    assertThat(useCase.listar(null, 0, 10)).isSameAs(vacia);
  }

  @Test
  void listarAceptaLosLimitesDelTamano() {
    when(gateway.buscarActivos(anyString(), anyInt(), anyInt()))
        .thenReturn(new Pagina<>(List.of(), 0, 1, 0));

    assertThat(useCase.listar("", 0, 1)).isNotNull();
    assertThat(useCase.listar("", 0, ClienteUseCase.TAMANO_MAXIMO)).isNotNull();
  }

  @Test
  void listarRechazaPaginacionFueraDeRango() {
    assertThatThrownBy(() -> useCase.listar("", -1, 10))
        .isInstanceOf(DatosInvalidosException.class);
    assertThatThrownBy(() -> useCase.listar("", 0, 0)).isInstanceOf(DatosInvalidosException.class);
    assertThatThrownBy(() -> useCase.listar("", 0, ClienteUseCase.TAMANO_MAXIMO + 1))
        .isInstanceOf(DatosInvalidosException.class)
        .hasMessageContaining("entre 1 y 50");
    verify(gateway, never()).buscarActivos(anyString(), anyInt(), anyInt());
  }

  @Test
  void obtenerDevuelveClienteActivo() {
    Cliente c = cliente(1L, "12345678-5");
    when(gateway.findById(1L)).thenReturn(Optional.of(c));

    assertThat(useCase.obtener(1L)).isSameAs(c);
  }

  @Test
  void obtenerFallaSiNoExiste() {
    when(gateway.findById(9L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> useCase.obtener(9L))
        .isInstanceOf(RecursoNoEncontradoException.class)
        .hasMessage("No existe un cliente con id 9");
  }

  @Test
  void obtenerFallaSiFueEliminado() {
    Cliente eliminado = cliente(1L, "12345678-5");
    eliminado.setActivo(false);
    when(gateway.findById(1L)).thenReturn(Optional.of(eliminado));

    assertThatThrownBy(() -> useCase.obtener(1L)).isInstanceOf(RecursoNoEncontradoException.class);
  }

  @Test
  void crearNormalizaElRutDescartaElIdYQuedaActivo() {
    Cliente nuevo = cliente(99L, "12.345.678-5");
    nuevo.setActivo(false);
    when(gateway.findByRut("12345678-5")).thenReturn(Optional.empty());
    when(gateway.save(any(Cliente.class)))
        .thenAnswer(
            inv -> {
              Cliente c = inv.getArgument(0);
              c.setId(1L);
              return c;
            });

    Cliente creado = useCase.crear(nuevo, USUARIO);

    ArgumentCaptor<Cliente> captor = ArgumentCaptor.forClass(Cliente.class);
    verify(gateway).save(captor.capture());
    assertThat(captor.getValue().getRut()).isEqualTo("12345678-5");
    assertThat(captor.getValue().isActivo()).isTrue();
    assertThat(creado.getId()).isEqualTo(1L);
  }

  @Test
  void crearNoPasaElIdEntranteAlGateway() {
    when(gateway.findByRut("12345678-5")).thenReturn(Optional.empty());
    when(gateway.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));

    assertThat(useCase.crear(cliente(99L, "12345678-5"), USUARIO).getId()).isNull();
  }

  @Test
  void crearFallaConRutInvalido() {
    assertThatThrownBy(() -> useCase.crear(cliente(null, "12.345.678-9"), USUARIO))
        .isInstanceOf(DatosInvalidosException.class)
        .hasMessageContaining("RUT no es válido");
    verify(gateway, never()).save(any(Cliente.class));
  }

  @Test
  void crearFallaConRutDuplicado() {
    when(gateway.findByRut("12345678-5")).thenReturn(Optional.of(cliente(1L, "12345678-5")));

    assertThatThrownBy(() -> useCase.crear(cliente(null, "12345678-5"), USUARIO))
        .isInstanceOf(RecursoDuplicadoException.class)
        .hasMessage("Ya existe un cliente con el RUT 12345678-5");
    verify(gateway, never()).save(any(Cliente.class));
  }

  @Test
  void actualizarCopiaTodosLosCampos() {
    Cliente actual = cliente(1L, "12345678-5");
    Cliente cambios =
        new Cliente(null, "76.123.456-0", "Nuevo", "nd", "Calle 2", "n@b.cl", "+56922222222", true);
    when(gateway.findById(1L)).thenReturn(Optional.of(actual));
    when(gateway.findByRut("76123456-0")).thenReturn(Optional.empty());
    when(gateway.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));

    Cliente r = useCase.actualizar(1L, cambios, USUARIO);

    assertThat(r.getId()).isEqualTo(1L);
    assertThat(r.getRut()).isEqualTo("76123456-0");
    assertThat(r.getNombre()).isEqualTo("Nuevo");
    assertThat(r.getDescripcion()).isEqualTo("nd");
    assertThat(r.getDireccion()).isEqualTo("Calle 2");
    assertThat(r.getEmail()).isEqualTo("n@b.cl");
    assertThat(r.getTelefono()).isEqualTo("+56922222222");
    verify(gateway).save(actual);
  }

  @Test
  void actualizarPermiteMantenerSuPropioRut() {
    Cliente actual = cliente(1L, "12345678-5");
    when(gateway.findById(1L)).thenReturn(Optional.of(actual));
    when(gateway.findByRut("12345678-5")).thenReturn(Optional.of(actual));
    when(gateway.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));

    assertThat(useCase.actualizar(1L, cliente(null, "12345678-5"), USUARIO).getId()).isEqualTo(1L);
  }

  @Test
  void actualizarFallaSiElRutEsDeOtroCliente() {
    when(gateway.findById(1L)).thenReturn(Optional.of(cliente(1L, "12345678-5")));
    when(gateway.findByRut("76123456-0")).thenReturn(Optional.of(cliente(2L, "76123456-0")));

    assertThatThrownBy(() -> useCase.actualizar(1L, cliente(null, "76123456-0"), USUARIO))
        .isInstanceOf(RecursoDuplicadoException.class);
    verify(gateway, never()).save(any(Cliente.class));
  }

  @Test
  void actualizarFallaConRutInvalido() {
    when(gateway.findById(1L)).thenReturn(Optional.of(cliente(1L, "12345678-5")));

    assertThatThrownBy(() -> useCase.actualizar(1L, cliente(null, "1-1"), USUARIO))
        .isInstanceOf(DatosInvalidosException.class);
    verify(gateway, never()).save(any(Cliente.class));
  }

  @Test
  void eliminarConConfirmacionDejaInactivo() {
    Cliente actual = cliente(1L, "12345678-5");
    when(gateway.findById(1L)).thenReturn(Optional.of(actual));

    useCase.eliminar(1L, true, USUARIO);

    ArgumentCaptor<Cliente> captor = ArgumentCaptor.forClass(Cliente.class);
    verify(gateway).save(captor.capture());
    assertThat(captor.getValue().isActivo()).isFalse();
  }

  @Test
  void eliminarSinConfirmacionNoHaceNada() {
    assertThatThrownBy(() -> useCase.eliminar(1L, false, USUARIO))
        .isInstanceOf(DatosInvalidosException.class)
        .hasMessageContaining("confirmar=true");
    verify(gateway, never()).findById(any());
    verify(gateway, never()).save(any(Cliente.class));
  }

  @Test
  void eliminarFallaSiNoExiste() {
    when(gateway.findById(9L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> useCase.eliminar(9L, true, USUARIO))
        .isInstanceOf(RecursoNoEncontradoException.class);
  }
}
