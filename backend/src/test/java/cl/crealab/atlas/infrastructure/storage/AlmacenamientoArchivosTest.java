package cl.crealab.atlas.infrastructure.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.UncheckedIOException;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** RF-5.3: storage interno con proteccion de path traversal. */
class AlmacenamientoArchivosTest {

  @TempDir Path directorio;

  @Test
  void guardaLeeYElimina() {
    AlmacenamientoArchivos almacenamiento = new AlmacenamientoArchivos(directorio.toString());
    String nombre = almacenamiento.guardar(new byte[] {1, 2, 3});
    assertThat(nombre).hasSize(36);
    assertThat(almacenamiento.leer(nombre)).containsExactly(1, 2, 3);
    assertThat(almacenamiento.eliminar(nombre)).isTrue();
    assertThat(almacenamiento.eliminar(nombre)).isFalse();
  }

  @Test
  void impidePathTraversal() {
    AlmacenamientoArchivos almacenamiento = new AlmacenamientoArchivos(directorio.toString());
    assertThatThrownBy(() -> almacenamiento.leer("../../etc/passwd"))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> almacenamiento.resolver("."))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void archivoInexistente() {
    AlmacenamientoArchivos almacenamiento = new AlmacenamientoArchivos(directorio.toString());
    assertThatThrownBy(() -> almacenamiento.leer("no-existe"))
        .isInstanceOf(UncheckedIOException.class);
  }
}
