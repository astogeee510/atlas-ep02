package cl.crealab.atlas.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.crealab.atlas.application.dto.NuevoUsuarioRequest;
import cl.crealab.atlas.application.dto.UsuarioEmpresaResponse;
import cl.crealab.atlas.domain.exception.ConflictoException;
import cl.crealab.atlas.domain.exception.NoEncontradoException;
import cl.crealab.atlas.domain.exception.ValidacionException;
import cl.crealab.atlas.domain.model.Membresia;
import cl.crealab.atlas.domain.model.Rol;
import cl.crealab.atlas.domain.model.Usuario;
import cl.crealab.atlas.domain.service.PoliticaContrasena;
import cl.crealab.atlas.infrastructure.persistence.MembresiaRepository;
import cl.crealab.atlas.infrastructure.persistence.UsuarioRepository;
import cl.crealab.atlas.soporte.Datos;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

/** CP-FUN-06: gestion de usuarios de la empresa y roles (RF-3.1, RF-3.3). */
@ExtendWith(MockitoExtension.class)
class UsuarioEmpresaServiceTest {

  @Mock private UsuarioRepository usuarios;
  @Mock private MembresiaRepository membresias;
  @Mock private ControlAcceso acceso;
  @Mock private PasswordEncoder encoder;
  @Mock private AuditoriaService auditoria;

  private UsuarioEmpresaService servicio;

  @BeforeEach
  void setUp() {
    servicio =
        new UsuarioEmpresaService(
            usuarios, membresias, acceso, encoder, new PoliticaContrasena(12), auditoria);
  }

  @Test
  void listaUsuariosDeLaEmpresa() {
    when(membresias.findByEmpresaId(1L)).thenReturn(List.of(new Membresia(1L, 2L, Rol.EDITOR)));
    when(usuarios.findById(2L)).thenReturn(Optional.of(Datos.usuario(2L, "editor")));
    List<UsuarioEmpresaResponse> lista = servicio.listar(1L, 1L);
    assertThat(lista).singleElement().extracting(UsuarioEmpresaResponse::rol).isEqualTo(Rol.EDITOR);
    verify(acceso).exigirAdmin(1L, 1L);
  }

  @Test
  void invitaUsuarioExistente() {
    when(usuarios.findByNombreUsuario("luis")).thenReturn(Optional.of(Datos.usuario(4L, "luis")));
    when(membresias.findByEmpresaIdAndUsuarioId(1L, 4L)).thenReturn(Optional.empty());
    when(membresias.save(any(Membresia.class))).thenAnswer(inv -> inv.getArgument(0));
    UsuarioEmpresaResponse r =
        servicio.agregar(1L, 1L, new NuevoUsuarioRequest("luis", null, null, Rol.EDITOR));
    assertThat(r.usuarioId()).isEqualTo(4L);
    assertThat(r.rol()).isEqualTo(Rol.EDITOR);
  }

  @Test
  void creaUsuarioNuevo() {
    when(usuarios.findByNombreUsuario("nuevo")).thenReturn(Optional.empty());
    when(usuarios.existsByCorreoIgnoreCase("nuevo@x.cl")).thenReturn(false);
    when(encoder.encode("Atlas#Demo2026")).thenReturn("hash");
    when(usuarios.save(any(Usuario.class)))
        .thenAnswer(
            inv -> {
              Usuario u = inv.getArgument(0);
              u.setId(8L);
              return u;
            });
    when(membresias.findByEmpresaIdAndUsuarioId(1L, 8L)).thenReturn(Optional.empty());
    when(membresias.save(any(Membresia.class))).thenAnswer(inv -> inv.getArgument(0));
    UsuarioEmpresaResponse r =
        servicio.agregar(
            1L, 1L, new NuevoUsuarioRequest("nuevo", "Nuevo@x.cl", "Atlas#Demo2026", Rol.ADMIN));
    assertThat(r.correo()).isEqualTo("nuevo@x.cl");
    assertThat(r.rol()).isEqualTo(Rol.ADMIN);
  }

  @Test
  void usuarioNuevoRequiereCorreoYContrasena() {
    when(usuarios.findByNombreUsuario("nuevo")).thenReturn(Optional.empty());
    assertThatThrownBy(
            () -> servicio.agregar(1L, 1L, new NuevoUsuarioRequest("nuevo", null, null, Rol.ADMIN)))
        .isInstanceOf(ValidacionException.class);
  }

  @Test
  void usuarioNuevoConContrasenaDebil() {
    when(usuarios.findByNombreUsuario("nuevo")).thenReturn(Optional.empty());
    assertThatThrownBy(
            () ->
                servicio.agregar(
                    1L, 1L, new NuevoUsuarioRequest("nuevo", "n@x.cl", "debil", Rol.ADMIN)))
        .isInstanceOf(ValidacionException.class);
    verify(usuarios, never()).save(any());
  }

  @Test
  void noDuplicaMembresia() {
    when(usuarios.findByNombreUsuario("luis")).thenReturn(Optional.of(Datos.usuario(4L, "luis")));
    when(membresias.findByEmpresaIdAndUsuarioId(1L, 4L))
        .thenReturn(Optional.of(new Membresia(1L, 4L, Rol.EDITOR)));
    assertThatThrownBy(
            () -> servicio.agregar(1L, 1L, new NuevoUsuarioRequest("luis", null, null, Rol.ADMIN)))
        .isInstanceOf(ConflictoException.class);
  }

  @Test
  void noDegradaAlUltimoAdministrador() {
    when(membresias.findByEmpresaIdAndUsuarioId(1L, 1L))
        .thenReturn(Optional.of(new Membresia(1L, 1L, Rol.ADMIN)));
    when(membresias.countByEmpresaIdAndRol(1L, Rol.ADMIN)).thenReturn(1L);
    assertThatThrownBy(() -> servicio.cambiarRol(1L, 1L, 1L, Rol.EDITOR))
        .isInstanceOf(ConflictoException.class);
  }

  @Test
  void promueveEditorAAdministrador() {
    Membresia membresia = new Membresia(1L, 2L, Rol.EDITOR);
    when(membresias.findByEmpresaIdAndUsuarioId(1L, 2L)).thenReturn(Optional.of(membresia));
    when(usuarios.findById(2L)).thenReturn(Optional.of(Datos.usuario(2L, "editor")));
    assertThat(servicio.cambiarRol(1L, 1L, 2L, Rol.ADMIN).rol()).isEqualTo(Rol.ADMIN);
    verify(membresias).save(membresia);
  }

  @Test
  void quitaAdministradorSiQuedaOtro() {
    Membresia membresia = new Membresia(1L, 3L, Rol.ADMIN);
    when(membresias.findByEmpresaIdAndUsuarioId(1L, 3L)).thenReturn(Optional.of(membresia));
    when(membresias.countByEmpresaIdAndRol(1L, Rol.ADMIN)).thenReturn(2L);
    servicio.eliminar(1L, 1L, 3L);
    verify(membresias).delete(membresia);
  }

  @Test
  void noQuitaUsuarioAjeno() {
    when(membresias.findByEmpresaIdAndUsuarioId(1L, 9L)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> servicio.eliminar(1L, 1L, 9L))
        .isInstanceOf(NoEncontradoException.class);
  }
}
