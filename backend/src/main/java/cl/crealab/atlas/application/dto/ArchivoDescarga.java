package cl.crealab.atlas.application.dto;

/**
 * Archivo listo para descargar.
 *
 * @param nombre nombre del archivo
 * @param tipoContenido tipo MIME
 * @param contenido bytes
 */
public record ArchivoDescarga(String nombre, String tipoContenido, byte[] contenido) {}
