package cl.duoc.atlas.domain.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ExcepcionesTest {

  @Test
  void losMensajesSonClaros() {
    assertThat(new RecursoNoEncontradoException("cliente", 7L))
        .hasMessage("No existe un cliente con id 7");
    assertThat(new RecursoDuplicadoException("dup")).hasMessage("dup");
    assertThat(new DatosInvalidosException("mal")).hasMessage("mal");
    assertThat(new CredencialesInvalidasException()).hasMessage("Usuario o contraseña incorrectos");
    assertThat(new CuentaBloqueadaException(15))
        .hasMessage("Demasiados intentos fallidos. Intente nuevamente en 15 minutos");
  }
}
