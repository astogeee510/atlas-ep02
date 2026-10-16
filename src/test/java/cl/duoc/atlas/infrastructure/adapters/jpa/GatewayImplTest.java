package cl.duoc.atlas.infrastructure.adapters.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.duoc.atlas.domain.model.Cliente;
import cl.duoc.atlas.domain.model.Contrato;
import cl.duoc.atlas.domain.model.Pagina;
import cl.duoc.atlas.domain.model.Rol;
import cl.duoc.atlas.domain.model.Usuario;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

class GatewayImplTest {

  private final ClienteMapper clienteMapper = new ClienteMapper();
  private final ContratoMapper contratoMapper = new ContratoMapper();
  private final UsuarioMapper usuarioMapper = new UsuarioMapper();

  private ClienteData clienteData(Long id) {
    return clienteMapper.toData(
        new Cliente(id, "12345678-5", "Ana", null, "Dir", "a@b.cl", "12345678", true));
  }

  @Test
  void clienteGatewayDelegaEnElRepositorio() {
    ClienteJpaRepository repo = mock(ClienteJpaRepository.class);
    ClienteGatewayImpl gateway = new ClienteGatewayImpl(repo, clienteMapper);
    when(repo.findById(1L)).thenReturn(Optional.of(clienteData(1L)));
    when(repo.findByRut("12345678-5")).thenReturn(Optional.of(clienteData(1L)));
    when(repo.save(any(ClienteData.class))).thenReturn(clienteData(5L));

    assertThat(gateway.findById(1L)).get().extracting(Cliente::getId).isEqualTo(1L);
    assertThat(gateway.findByRut("12345678-5")).isPresent();
    assertThat(gateway.save(new Cliente()).getId()).isEqualTo(5L);
  }

  @Test
  void clienteGatewayBuscaPaginadoYOrdenadoPorNombre() {
    ClienteJpaRepository repo = mock(ClienteJpaRepository.class);
    ClienteGatewayImpl gateway = new ClienteGatewayImpl(repo, clienteMapper);
    when(repo.findByActivoTrueAndNombreContainingIgnoreCase(eq("an"), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(clienteData(1L)), PageRequest.of(2, 3), 7));

    Pagina<Cliente> pagina = gateway.buscarActivos("an", 2, 3);

    ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
    verify(repo).findByActivoTrueAndNombreContainingIgnoreCase(eq("an"), captor.capture());
    assertThat(captor.getValue()).isEqualTo(PageRequest.of(2, 3, Sort.by("nombre")));
    assertThat(pagina.contenido()).hasSize(1);
    assertThat(pagina.pagina()).isEqualTo(2);
    assertThat(pagina.tamano()).isEqualTo(3);
    assertThat(pagina.totalElementos()).isEqualTo(7);
  }

  @Test
  void contratoGatewayDelegaEnElRepositorio() {
    ContratoJpaRepository repo = mock(ContratoJpaRepository.class);
    ContratoGatewayImpl gateway = new ContratoGatewayImpl(repo, contratoMapper);
    ContratoData data =
        contratoMapper.toData(
            new Contrato(4L, 1L, "T", null, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 1)));
    when(repo.findById(4L)).thenReturn(Optional.of(data));
    when(repo.findByClienteIdOrderByFechaTerminoAsc(1L)).thenReturn(List.of(data));
    when(repo.save(any(ContratoData.class))).thenReturn(data);

    assertThat(gateway.findById(4L)).get().extracting(Contrato::getTitulo).isEqualTo("T");
    assertThat(gateway.findByClienteId(1L)).hasSize(1);
    assertThat(gateway.save(new Contrato()).getId()).isEqualTo(4L);
    gateway.deleteById(4L);
    verify(repo).deleteById(4L);
  }

  @Test
  void usuarioGatewayDelegaEnElRepositorio() {
    UsuarioJpaRepository repo = mock(UsuarioJpaRepository.class);
    UsuarioGatewayImpl gateway = new UsuarioGatewayImpl(repo, usuarioMapper);
    UsuarioData data = usuarioMapper.toData(new Usuario(1L, "admin", "h", Rol.ADMIN, true));
    when(repo.findByNombreUsuario("admin")).thenReturn(Optional.of(data));
    when(repo.save(any(UsuarioData.class))).thenReturn(data);

    assertThat(gateway.findByNombreUsuario("admin"))
        .get()
        .extracting(Usuario::getRol)
        .isEqualTo(Rol.ADMIN);
    assertThat(gateway.save(new Usuario()).getNombreUsuario()).isEqualTo("admin");
  }
}
