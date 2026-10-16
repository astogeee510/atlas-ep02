package cl.crealab.atlas.infrastructure.crypto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Base64;
import org.junit.jupiter.api.Test;

/** CP-SEG-03 / NFR-SEG-3: cifrado AES-256-GCM de datos sensibles en reposo. */
class AesGcmCifradorTest {

  private static final byte[] CLAVE = new byte[32];

  private final AesGcmCifrador cifrador = new AesGcmCifrador(CLAVE);

  @Test
  void cifraYDescifra() {
    String cifrado = cifrador.cifrar("123456789");
    assertThat(cifrado).startsWith("v1:").doesNotContain("123456789");
    assertThat(cifrador.descifrar(cifrado)).isEqualTo("123456789");
  }

  @Test
  void cadaCifradoUsaUnIvDistinto() {
    assertThat(cifrador.cifrar("dato")).isNotEqualTo(cifrador.cifrar("dato"));
  }

  @Test
  void valorLegadoSinPrefijoSeDevuelveIgual() {
    assertThat(cifrador.descifrar("texto plano")).isEqualTo("texto plano");
    assertThat(AesGcmCifrador.estaCifrado(null)).isFalse();
  }

  @Test
  void detectaManipulacionDelTextoCifrado() {
    String cifrado = cifrador.cifrar("123456789");
    byte[] datos = Base64.getDecoder().decode(cifrado.substring(3));
    datos[datos.length - 1] ^= 1;
    String manipulado = "v1:" + Base64.getEncoder().encodeToString(datos);
    assertThatThrownBy(() -> cifrador.descifrar(manipulado))
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void otraClaveNoPuedeDescifrar() {
    byte[] otra = new byte[32];
    otra[0] = 1;
    String cifrado = cifrador.cifrar("secreto");
    assertThatThrownBy(() -> new AesGcmCifrador(otra).descifrar(cifrado))
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void exigeClaveDe32Bytes() {
    assertThatThrownBy(() -> new AesGcmCifrador(new byte[16]))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new AesGcmCifrador(null)).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void leeLaClaveDesdeLaPropiedadDeSistema() {
    String anterior = System.getProperty(AesGcmCifrador.PROPIEDAD_CLAVE);
    try {
      System.setProperty(AesGcmCifrador.PROPIEDAD_CLAVE, Base64.getEncoder().encodeToString(CLAVE));
      String cifrado = AesGcmCifrador.desdeEntorno().cifrar("x");
      assertThat(cifrador.descifrar(cifrado)).isEqualTo("x");
    } finally {
      if (anterior == null) {
        System.clearProperty(AesGcmCifrador.PROPIEDAD_CLAVE);
      } else {
        System.setProperty(AesGcmCifrador.PROPIEDAD_CLAVE, anterior);
      }
    }
  }

  @Test
  void converterCifraYDescifraColumnas() {
    CampoCifradoConverter converter = new CampoCifradoConverter();
    String columna = converter.convertToDatabaseColumn("cuenta");
    assertThat(columna).startsWith("v1:");
    assertThat(converter.convertToEntityAttribute(columna)).isEqualTo("cuenta");
    assertThat(converter.convertToDatabaseColumn(null)).isNull();
    assertThat(converter.convertToEntityAttribute(null)).isNull();
  }
}
