package cl.crealab.atlas.bdd;

import static org.assertj.core.api.Assertions.assertThat;

import cl.crealab.atlas.domain.service.PoliticaContrasena;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Entonces;
import java.util.List;

/** Pasos de la politica de contrasenas. */
public class PoliticaContrasenaSteps {

  private final PoliticaContrasena politica = new PoliticaContrasena(12);
  private List<String> errores;

  @Cuando("valido la contraseña {string}")
  public void validoLaContrasena(String contrasena) {
    errores = politica.validar(contrasena);
  }

  @Entonces("la contraseña es {string}")
  public void laContrasenaEs(String resultado) {
    assertThat(errores.isEmpty()).isEqualTo("aceptada".equals(resultado));
  }
}
