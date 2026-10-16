package cl.crealab.atlas.application.dto;

import static org.assertj.core.api.Assertions.assertThat;

import cl.crealab.atlas.domain.model.Rol;
import cl.crealab.atlas.soporte.Datos;
import org.junit.jupiter.api.Test;

/** Conversiones de DTO y minimizacion de datos (Ley 19.628). */
class DtoTest {

  @Test
  void enmascaraNumeroDeCuenta() {
    assertThat(ClienteResponse.enmascarar("123456789")).isEqualTo("****6789");
    assertThat(ClienteResponse.enmascarar("1234")).isEqualTo("****");
    assertThat(ClienteResponse.enmascarar(null)).isNull();
  }

  @Test
  void clienteParaEditorYAdministrador() {
    assertThat(ClienteResponse.desde(Datos.cliente(1L, 1L), Rol.EDITOR).numeroCuenta())
        .isEqualTo("****6789");
    assertThat(ClienteResponse.desde(Datos.cliente(1L, 1L), Rol.ADMIN).numeroCuenta())
        .isEqualTo("123456789");
  }

  @Test
  void paginacionSegura() {
    assertThat(PaginaResponse.tamanoSeguro(0)).isEqualTo(1);
    assertThat(PaginaResponse.tamanoSeguro(51)).isEqualTo(50);
    assertThat(PaginaResponse.tamanoSeguro(10)).isEqualTo(10);
    assertThat(PaginaResponse.paginaSegura(-1)).isZero();
    assertThat(PaginaResponse.paginaSegura(3)).isEqualTo(3);
  }

  @Test
  void respuestasDesdeEntidades() {
    assertThat(UsuarioResponse.desde(Datos.usuario(1L, "ana")).nombreUsuario()).isEqualTo("ana");
    assertThat(EmpresaResponse.desde(Datos.empresa(2L)).id()).isEqualTo(2L);
    assertThat(DocumentoResponse.desde(Datos.documento(3L, 4L)).contratoId()).isEqualTo(4L);
  }
}
