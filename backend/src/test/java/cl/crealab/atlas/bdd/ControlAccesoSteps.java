package cl.crealab.atlas.bdd;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cl.crealab.atlas.application.ControlAcceso;
import cl.crealab.atlas.domain.exception.AccesoDenegadoException;
import cl.crealab.atlas.domain.model.Membresia;
import cl.crealab.atlas.domain.model.Rol;
import cl.crealab.atlas.infrastructure.persistence.MembresiaRepository;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import java.util.Optional;

/** Pasos del control de acceso por rol y empresa. */
public class ControlAccesoSteps {

  private static final Long USUARIO = 10L;

  private final MembresiaRepository membresias = mock(MembresiaRepository.class);
  private final ControlAcceso acceso = new ControlAcceso(membresias);
  private boolean permitida;

  @Dado("un usuario con rol {string} en la empresa {long}")
  public void unUsuarioConRol(String rol, Long empresaId) {
    when(membresias.findByEmpresaIdAndUsuarioId(empresaId, USUARIO))
        .thenReturn(Optional.of(new Membresia(empresaId, USUARIO, Rol.valueOf(rol))));
  }

  @Cuando("intenta escribir datos de la empresa {long}")
  public void intentaEscribir(Long empresaId) {
    try {
      acceso.exigirAdmin(USUARIO, empresaId);
      permitida = true;
    } catch (AccesoDenegadoException e) {
      permitida = false;
    }
  }

  @Entonces("la operación es {string}")
  public void laOperacionEs(String resultado) {
    assertThat(permitida).isEqualTo("permitida".equals(resultado));
  }
}
