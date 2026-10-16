package cl.crealab.atlas.infrastructure.storage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Storage interno de documentos (RF-5.3). Los archivos se guardan con un nombre UUID (nunca con el
 * nombre original) fuera de cualquier carpeta publica, y la BD guarda solo la referencia.
 */
@Component
public class AlmacenamientoArchivos {

  private final Path directorio;

  public AlmacenamientoArchivos(@Value("${atlas.storage.dir}") String directorio) {
    this.directorio = Path.of(directorio).toAbsolutePath().normalize();
  }

  /**
   * Guarda un archivo.
   *
   * @param contenido bytes del archivo
   * @return nombre interno con el que quedo guardado
   */
  public String guardar(byte[] contenido) {
    String nombre = UUID.randomUUID().toString();
    try {
      Files.createDirectories(directorio);
      Files.write(resolver(nombre), contenido);
      return nombre;
    } catch (IOException e) {
      throw new UncheckedIOException("No fue posible guardar el documento", e);
    }
  }

  /**
   * Lee un archivo guardado.
   *
   * @param nombre nombre interno
   * @return bytes del archivo
   */
  public byte[] leer(String nombre) {
    try {
      return Files.readAllBytes(resolver(nombre));
    } catch (IOException e) {
      throw new UncheckedIOException("No fue posible leer el documento", e);
    }
  }

  /**
   * Elimina un archivo si existe.
   *
   * @param nombre nombre interno
   * @return true si el archivo existia
   */
  public boolean eliminar(String nombre) {
    try {
      return Files.deleteIfExists(resolver(nombre));
    } catch (IOException e) {
      throw new UncheckedIOException("No fue posible eliminar el documento", e);
    }
  }

  /** Resuelve el nombre dentro del directorio impidiendo path traversal. */
  Path resolver(String nombre) {
    Path ruta = directorio.resolve(nombre).normalize();
    if (!ruta.startsWith(directorio) || ruta.equals(directorio)) {
      throw new IllegalArgumentException("Ruta de documento invalida");
    }
    return ruta;
  }
}
