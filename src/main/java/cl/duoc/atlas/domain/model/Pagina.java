package cl.duoc.atlas.domain.model;

import java.util.List;

/**
 * Pagina de resultados (RF-4.2: listado con paginacion).
 *
 * @param contenido elementos de la pagina
 * @param pagina numero de pagina (desde 0)
 * @param tamano cantidad maxima de elementos por pagina
 * @param totalElementos total de elementos que cumplen el filtro
 * @param <T> tipo de elemento
 */
public record Pagina<T>(List<T> contenido, int pagina, int tamano, long totalElementos) {

  public Pagina {
    contenido = List.copyOf(contenido);
  }

  /**
   * Calcula la cantidad de paginas.
   *
   * @return total de paginas (0 si no hay elementos)
   */
  public int totalPaginas() {
    return (int) ((totalElementos + tamano - 1) / tamano);
  }
}
