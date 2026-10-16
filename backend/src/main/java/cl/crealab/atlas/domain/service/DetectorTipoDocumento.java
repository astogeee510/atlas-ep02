package cl.crealab.atlas.domain.service;

import java.util.Locale;
import java.util.Optional;

/**
 * Determina el tipo real de un archivo leyendo su firma binaria y exige que la extension sea
 * coherente con ella. Asi un ejecutable renombrado a .pdf es rechazado (NFR-SEG-5).
 */
public final class DetectorTipoDocumento {

  private static final byte[] FIRMA_PDF = {0x25, 0x50, 0x44, 0x46};
  private static final byte[] FIRMA_PNG = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
  private static final byte[] FIRMA_JPG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
  private static final byte[] FIRMA_OLE = {
    (byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0, (byte) 0xA1, (byte) 0xB1, 0x1A, (byte) 0xE1
  };
  private static final byte[] FIRMA_ZIP = {0x50, 0x4B, 0x03, 0x04};

  private DetectorTipoDocumento() {}

  /**
   * Detecta el tipo de documento.
   *
   * @param contenido bytes del archivo
   * @param nombreArchivo nombre original (para validar la extension)
   * @return el tipo detectado, o vacio si la firma no es permitida o no coincide con la extension
   */
  public static Optional<TipoDocumento> detectar(byte[] contenido, String nombreArchivo) {
    Optional<TipoDocumento> porFirma = detectarPorFirma(contenido);
    String extension = extension(nombreArchivo);
    return porFirma.filter(tipo -> tipo.getExtensiones().contains(extension));
  }

  /**
   * Detecta el tipo solo por la firma binaria.
   *
   * @param contenido bytes del archivo
   * @return el tipo si la firma corresponde a uno permitido
   */
  public static Optional<TipoDocumento> detectarPorFirma(byte[] contenido) {
    if (comienzaCon(contenido, FIRMA_PDF)) {
      return Optional.of(TipoDocumento.PDF);
    }
    if (comienzaCon(contenido, FIRMA_PNG)) {
      return Optional.of(TipoDocumento.PNG);
    }
    if (comienzaCon(contenido, FIRMA_JPG)) {
      return Optional.of(TipoDocumento.JPG);
    }
    if (comienzaCon(contenido, FIRMA_OLE)) {
      return Optional.of(TipoDocumento.OFFICE_BINARIO);
    }
    if (comienzaCon(contenido, FIRMA_ZIP)) {
      return Optional.of(TipoDocumento.OFFICE_XML);
    }
    return Optional.empty();
  }

  /**
   * Obtiene la extension en minusculas.
   *
   * @param nombreArchivo nombre del archivo
   * @return extension sin punto, o cadena vacia
   */
  public static String extension(String nombreArchivo) {
    if (nombreArchivo == null) {
      return "";
    }
    int punto = nombreArchivo.lastIndexOf('.');
    if (punto < 0 || punto == nombreArchivo.length() - 1) {
      return "";
    }
    return nombreArchivo.substring(punto + 1).toLowerCase(Locale.ROOT);
  }

  private static boolean comienzaCon(byte[] contenido, byte[] firma) {
    if (contenido == null || contenido.length < firma.length) {
      return false;
    }
    for (int i = 0; i < firma.length; i++) {
      if (contenido[i] != firma[i]) {
        return false;
      }
    }
    return true;
  }
}
