package cl.crealab.atlas.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import cl.crealab.atlas.domain.exception.AccesoDenegadoException;
import cl.crealab.atlas.domain.model.Membresia;
import cl.crealab.atlas.domain.model.Rol;
import cl.crealab.atlas.infrastructure.persistence.MembresiaRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** CP-SEG-01 / CP-INT-04: control de acceso por rol y empresa (NFR-SEG-4, RB-2, RB-4). */
@ExtendWith(MockitoExtension.class)
class ControlAccesoTest {

  @Mock private MembresiaRepository membresias;

  private ControlAcceso acceso;

  @BeforeEach
  void setUp() {
    acceso = new ControlAcceso(membresias);
  }

  @Test
  void miembroObtieneSuRol() {
    when(membresias.findByEmpresaIdAndUsuarioId(1L, 2L))
        .thenReturn(Optional.of(new Membresia(1L, 2L, Rol.EDITOR)));
    assertThat(acceso.exigirMiembro(2L, 1L)).isEqualTo(Rol.EDITOR);
  }

  @Test
  void usuarioDeOtraEmpresaEsRechazado() {
    when(membresias.findByEmpresaIdAndUsuarioId(1L, 3L)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> acceso.exigirMiembro(3L, 1L))
        .isInstanceOf(AccesoDenegadoException.class);
  }

  @Test
  void editorNoPuedeEscribir() {
    when(membresias.findByEmpresaIdAndUsuarioId(1L, 2L))
        .thenReturn(Optional.of(new Membresia(1L, 2L, Rol.EDITOR)));
    assertThatThrownBy(() -> acceso.exigirAdmin(2L, 1L))
        .isInstanceOf(AccesoDenegadoException.class)
        .hasMessageContaining("administrador");
  }

  @Test
  void administradorPuedeEscribir() {
    when(membresias.findByEmpresaIdAndUsuarioId(1L, 1L))
        .thenReturn(Optional.of(new Membresia(1L, 1L, Rol.ADMIN)));
    assertThatCode(() -> acceso.exigirAdmin(1L, 1L)).doesNotThrowAnyException();
  }
}
