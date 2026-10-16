package cl.duoc.atlas.bdd;

import static org.assertj.core.api.Assertions.assertThat;

import cl.duoc.atlas.application.usecase.AuthUseCase;
import cl.duoc.atlas.domain.exception.CuentaBloqueadaException;
import cl.duoc.atlas.domain.model.Rol;
import cl.duoc.atlas.domain.model.Sesion;
import cl.duoc.atlas.domain.model.Usuario;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import java.time.Clock;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/** Step definitions de autenticacion.feature. Usa bcrypt real (costo bajo para rapidez). */
public class AutenticacionSteps {

  private final MemoriaGateways.Usuarios usuarios = new MemoriaGateways.Usuarios();
  private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(4);
  private final AuthUseCase useCase =
      new AuthUseCase(usuarios, encoder, new MemoriaGateways.Tokens(), Clock.systemUTC());

  private String nombre;
  private Sesion sesion;

  private RuntimeException error;

  private void ejecutar(Runnable accion) {
    error = null;
    try {
      accion.run();
    } catch (RuntimeException e) {
      error = e;
    }
  }

  @Dado("un usuario {string} con contraseña {string} y rol {string}")
  public void unUsuario(String usuario, String contrasena, String rol) {
    nombre = usuario;
    usuarios.save(new Usuario(null, usuario, encoder.encode(contrasena), Rol.valueOf(rol), true));
  }

  @Cuando("inicia sesión con la contraseña {string}")
  public void iniciaSesion(String contrasena) {
    ejecutar(() -> sesion = useCase.login(nombre, contrasena));
  }

  @Cuando("falla el inicio de sesión {int} veces")
  public void fallaVeces(int veces) {
    for (int i = 0; i < veces; i++) {
      ejecutar(() -> useCase.login(nombre, "incorrecta"));
    }
  }

  @Entonces("recibe un token con rol {string}")
  public void recibeToken(String rol) {
    assertThat(sesion).isNotNull();
    assertThat(sesion.token()).isNotBlank();
    assertThat(sesion.rol().name()).isEqualTo(rol);
  }

  @Entonces("la cuenta queda bloqueada")
  public void cuentaBloqueada() {
    assertThat(error).isInstanceOf(CuentaBloqueadaException.class);
    assertThat(sesion).isNull();
  }
}
