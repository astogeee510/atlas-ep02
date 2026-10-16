package cl.crealab.atlas.application.dto;

import java.util.List;
import java.util.function.Function;
import org.springframework.data.domain.Page;

/**
 * Pagina de resultados (RF-4.2: listado con paginacion).
 *
 * @param contenido elementos de la pagina
 * @param pagina numero de pagina (desde 0)
 * @param tamano tamano de pagina
 * @param totalElementos total de elementos
 * @param totalPaginas total de paginas
 * @param <T> tipo de elemento
 */
public record PaginaResponse<T>(
    List<T> contenido, int pagina, int tamano, long totalElementos, int totalPaginas) {

  /** Tamano maximo de pagina permitido. */
  public static final int TAMANO_MAXIMO = 50;

  /** Convierte una pagina de Spring Data. */
  public static <E, T> PaginaResponse<T> de(Page<E> page, Function<E, T> mapeo) {
    return new PaginaResponse<>(
        page.getContent().stream().map(mapeo).toList(),
        page.getNumber(),
        page.getSize(),
        page.getTotalElements(),
        page.getTotalPages());
  }

  /** Limita el tamano solicitado al rango 1..50. */
  public static int tamanoSeguro(int tamano) {
    return Math.max(1, Math.min(tamano, TAMANO_MAXIMO));
  }

  /** Evita numeros de pagina negativos. */
  public static int paginaSegura(int pagina) {
    return Math.max(0, pagina);
  }
}
