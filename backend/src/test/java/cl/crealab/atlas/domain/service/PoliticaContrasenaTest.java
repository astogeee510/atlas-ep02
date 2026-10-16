package cl.crealab.atlas.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

/** CP-SEG-03: politica de contrasenas del ERS 2.4. */
class PoliticaContrasenaTest {

  private final PoliticaContrasena politica = new PoliticaContrasena(12);

  @Test
  void aceptaContrasenaQueCumpleTodo() {
    assertThat(politica.validar("Atlas#Demo2026")).isEmpty();
  }

  @Test
  void rechazaContrasenaCorta() {
    assertThat(politica.validar("Ab1#cdefgh")).containsExactly("Debe tener al menos 12 caracteres");
  }

  @Test
  void aceptaExactamenteElLargoMinimo() {
    assertThat(politica.validar("Abcdefgh1#xy")).isEmpty();
  }

  @Test
  void exigeMayuscula() {
    assertThat(politica.validar("atlas#demo2026"))
        .containsExactly("Debe incluir al menos una letra mayuscula");
  }

  @Test
  void exigeMinuscula() {
    assertThat(politica.validar("ATLAS#DEMO2026"))
        .containsExactly("Debe incluir al menos una letra minuscula");
  }

  @Test
  void exigeNumero() {
    assertThat(politica.validar("Atlas#DemoDemo"))
        .containsExactly("Debe incluir al menos un numero");
  }

  @Test
  void exigeSimbolo() {
    assertThat(politica.validar("AtlasDemo2026x"))
        .containsExactly("Debe incluir al menos un simbolo");
  }

  @Test
  void contrasenaNulaIncumpleTodasLasReglas() {
    List<String> errores = politica.validar(null);
    assertThat(errores).hasSize(5);
    assertThat(politica.getLargoMinimo()).isEqualTo(12);
  }
}
