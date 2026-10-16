package cl.duoc.atlas.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.duoc.atlas.domain.gateway.ClienteGateway;
import cl.duoc.atlas.domain.gateway.ContratoGateway;
import cl.duoc.atlas.domain.gateway.UsuarioGateway;
import cl.duoc.atlas.domain.model.Cliente;
import cl.duoc.atlas.domain.model.Contrato;
import cl.duoc.atlas.domain.model.Rol;
import cl.duoc.atlas.domain.model.Usuario;
import java.time.Clock;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

class DatosInicialesTest {

  private final UsuarioGateway usuarios = mock(UsuarioGateway.class);
  private final ClienteGateway clientes = mock(ClienteGateway.class);
  private final ContratoGateway contratos = mock(ContratoGateway.class);
  private final PasswordEncoder encoder = mock(PasswordEncoder.class);

  private DatosIniciales datos(boolean demo) {
    return new DatosIniciales(
        usuarios, clientes, contratos, encoder, Clock.systemUTC(), "pa", "pe", demo);
  }

  @Test
  void creaAdminYEditorConHashCuandoNoExisten() {
    when(usuarios.findByNombreUsuario(anyString())).thenReturn(Optional.empty());
    when(encoder.encode("pa")).thenReturn("hashA");
    when(encoder.encode("pe")).thenReturn("hashE");

    datos(false).run(null);

    ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
    verify(usuarios, times(2)).save(captor.capture());
    assertThat(captor.getAllValues())
        .extracting(Usuario::getNombreUsuario, Usuario::getHashContrasena, Usuario::getRol)
        .containsExactly(
            org.assertj.core.groups.Tuple.tuple("admin", "hashA", Rol.ADMIN),
            org.assertj.core.groups.Tuple.tuple("editor", "hashE", Rol.EDITOR));
    verify(clientes, never()).save(any());
  }

  @Test
  void noDuplicaUsuariosExistentes() {
    when(usuarios.findByNombreUsuario(anyString())).thenReturn(Optional.of(new Usuario()));

    datos(false).run(null);

    verify(usuarios, never()).save(any());
  }

  @Test
  void cargaDatosDeDemostracionUnaSolaVez() {
    when(usuarios.findByNombreUsuario(anyString())).thenReturn(Optional.of(new Usuario()));
    when(clientes.findByRut("76123456-0")).thenReturn(Optional.empty());
    when(clientes.save(any(Cliente.class)))
        .thenAnswer(
            inv -> {
              Cliente c = inv.getArgument(0);
              c.setId(1L);
              return c;
            });

    datos(true).run(null);

    verify(clientes, times(3)).save(any(Cliente.class));
    verify(contratos, times(3)).save(any(Contrato.class));
  }

  @Test
  void noRecargaLaDemoSiYaExiste() {
    when(usuarios.findByNombreUsuario(anyString())).thenReturn(Optional.of(new Usuario()));
    when(clientes.findByRut("76123456-0")).thenReturn(Optional.of(new Cliente()));

    datos(true).run(null);

    verify(clientes, never()).save(any(Cliente.class));
  }
}
