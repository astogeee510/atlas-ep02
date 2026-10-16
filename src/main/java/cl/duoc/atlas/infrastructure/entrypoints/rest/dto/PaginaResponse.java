package cl.duoc.atlas.infrastructure.entrypoints.rest.dto;

import cl.duoc.atlas.domain.model.Pagina;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.function.Function;

/**
 * Pagina de resultados devuelta por la API (RF-4.2).
 *
 * @param contenido elementos
 * @param pagina pagina actual desde 0
 * @param tamano elementos por pagina
 * @param totalElementos total de elementos
 * @param totalPaginas total de paginas
 * @param <T> tipo de elemento
 */
@Schema(description = "Página de resultados")
public record PaginaResponse<T>(
    List<T> contenido, int pagina, int tamano, long totalElementos, int totalPaginas) {

  public PaginaResponse {
    contenido = List.copyOf(contenido);
  }

  public static <D, T> PaginaResponse<T> fromDomain(Pagina<D> pagina, Function<D, T> conversor) {
    return new PaginaResponse<>(
        pagina.contenido().stream().map(conversor).toList(),
        pagina.pagina(),
        pagina.tamano(),
        pagina.totalElementos(),
        pagina.totalPaginas());
  }
}
