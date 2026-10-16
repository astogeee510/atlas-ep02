package cl.crealab.atlas.infrastructure.crypto;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cifrado simetrico AES-256-GCM para datos sensibles en reposo (NFR-SEG-3). Cada valor usa un IV
 * aleatorio de 12 bytes; el resultado se guarda como {@code v1:base64(iv + cifrado + tag)}.
 */
public class AesGcmCifrador {

  /** Variable de entorno con la clave en Base64 (32 bytes). */
  public static final String VARIABLE_CLAVE = "ATLAS_CRYPTO_KEY";

  /** Propiedad de sistema alternativa (util en pruebas). */
  public static final String PROPIEDAD_CLAVE = "atlas.crypto.key";

  private static final Logger LOG = LoggerFactory.getLogger(AesGcmCifrador.class);
  private static final String PREFIJO = "v1:";
  private static final int LARGO_IV = 12;
  private static final int LARGO_TAG_BITS = 128;
  private static final int LARGO_CLAVE = 32;
  private static final String CLAVE_DESARROLLO = "c29sby1wYXJhLWRlc2Fycm9sbG8tbm8tdXNhci1xYSE=";
  private static final SecureRandom ALEATORIO = new SecureRandom();

  private final SecretKey clave;

  /**
   * Crea el cifrador con una clave de 32 bytes.
   *
   * @param claveBytes clave AES-256
   */
  public AesGcmCifrador(byte[] claveBytes) {
    if (claveBytes == null || claveBytes.length != LARGO_CLAVE) {
      throw new IllegalArgumentException("La clave AES debe tener 32 bytes");
    }
    this.clave = new SecretKeySpec(claveBytes.clone(), "AES");
  }

  /**
   * Crea el cifrador leyendo la clave desde {@value #VARIABLE_CLAVE} o {@value #PROPIEDAD_CLAVE}.
   * Si no existe se usa una clave de desarrollo y se advierte en el log.
   *
   * @return cifrador listo para usar
   */
  public static AesGcmCifrador desdeEntorno() {
    String claveBase64 = System.getenv(VARIABLE_CLAVE);
    if (claveBase64 == null || claveBase64.isBlank()) {
      claveBase64 = System.getProperty(PROPIEDAD_CLAVE);
    }
    if (claveBase64 == null || claveBase64.isBlank()) {
      LOG.warn("ATLAS_CRYPTO_KEY no definida: se usa la clave de DESARROLLO (no usar en QA/prod)");
      claveBase64 = CLAVE_DESARROLLO;
    }
    return new AesGcmCifrador(Base64.getDecoder().decode(claveBase64.trim()));
  }

  /**
   * Cifra un texto.
   *
   * @param textoPlano valor a proteger
   * @return valor cifrado con prefijo de version
   */
  public String cifrar(String textoPlano) {
    try {
      byte[] iv = new byte[LARGO_IV];
      ALEATORIO.nextBytes(iv);
      Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
      cipher.init(Cipher.ENCRYPT_MODE, clave, new GCMParameterSpec(LARGO_TAG_BITS, iv));
      byte[] cifrado = cipher.doFinal(textoPlano.getBytes(StandardCharsets.UTF_8));
      ByteBuffer buffer = ByteBuffer.allocate(iv.length + cifrado.length);
      buffer.put(iv).put(cifrado);
      return PREFIJO + Base64.getEncoder().encodeToString(buffer.array());
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException("No fue posible cifrar el dato", e);
    }
  }

  /**
   * Descifra un valor producido por {@link #cifrar(String)}. Un valor sin prefijo se considera
   * legado (texto plano) y se devuelve tal cual.
   *
   * @param valor valor almacenado
   * @return texto plano
   */
  public String descifrar(String valor) {
    if (!estaCifrado(valor)) {
      return valor;
    }
    try {
      byte[] datos = Base64.getDecoder().decode(valor.substring(PREFIJO.length()));
      ByteBuffer buffer = ByteBuffer.wrap(datos);
      byte[] iv = new byte[LARGO_IV];
      buffer.get(iv);
      byte[] cifrado = new byte[buffer.remaining()];
      buffer.get(cifrado);
      Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
      cipher.init(Cipher.DECRYPT_MODE, clave, new GCMParameterSpec(LARGO_TAG_BITS, iv));
      return new String(cipher.doFinal(cifrado), StandardCharsets.UTF_8);
    } catch (GeneralSecurityException | IllegalArgumentException e) {
      throw new IllegalStateException("No fue posible descifrar el dato", e);
    }
  }

  /**
   * Indica si un valor almacenado esta cifrado.
   *
   * @param valor valor almacenado
   * @return true si tiene el prefijo de version
   */
  public static boolean estaCifrado(String valor) {
    return valor != null && valor.startsWith(PREFIJO);
  }
}
