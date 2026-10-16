package cl.duoc.atlas.infrastructure.entrypoints.rest.dto;

import static org.assertj.core.api.Assertions.assertThat;

import cl.duoc.atlas.domain.model.Cliente;
import cl.duoc.atlas.domain.model.Contrato;
import cl.duoc.atlas.domain.model.EstadoContrato;
import cl.duoc.atlas.domain.model.Pagina;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class DtoTest {

  @Test
  void clienteRequestYResponse() {
    Cliente c = new ClienteRequest("12345678-5", "N", "D", "Dir", "a@b.cl", "12345678").toDomain();
    c.setId(1L);

    ClienteResponse r = ClienteResponse.fromDomain(c);

    assertThat(r)
        .isEqualTo(new ClienteResponse(1L, "12345678-5", "N", "D", "Dir", "a@b.cl", "12345678"));
    assertThat(c.isActivo()).isTrue();
  }

  @Test
  void contratoRequestYResponse() {
    LocalDate i = LocalDate.of(2026, 1, 1);
    LocalDate t = LocalDate.of(2026, 2, 1);
    Contrato c = new ContratoRequest("T", "D", i, t).toDomain();

    assertThat(c.getId()).isNull();
    assertThat(c.getClienteId()).isNull();
    assertThat(ContratoResponse.fromDomain(c, EstadoContrato.ACTIVO))
        .isEqualTo(new ContratoResponse(null, null, "T", "D", i, t, EstadoContrato.ACTIVO));
  }

  @Test
  void paginaResponseConvierteElContenido() {
    PaginaResponse<String> r =
        PaginaResponse.fromDomain(new Pagina<>(List.of(1, 2), 1, 2, 5), String::valueOf);

    assertThat(r.contenido()).containsExactly("1", "2");
    assertThat(r.pagina()).isEqualTo(1);
    assertThat(r.tamano()).isEqualTo(2);
    assertThat(r.totalElementos()).isEqualTo(5);
    assertThat(r.totalPaginas()).isEqualTo(3);
  }

  @Test
  void errorResponseExponeSusCampos() {
    ErrorResponse e = new ErrorResponse(400, "Bad Request", "m", "/p");
    LocalDateTime ahora = LocalDateTime.of(2026, 10, 16, 12, 0);
    e.setTimestamp(ahora);
    e.setStatus(404);
    e.setError("Not Found");
    e.setMessage("otro");
    e.setPath("/q");
    e.setDetails(Map.of("campo", "error"));

    assertThat(e.getTimestamp()).isEqualTo(ahora);
    assertThat(e.getStatus()).isEqualTo(404);
    assertThat(e.getError()).isEqualTo("Not Found");
    assertThat(e.getMessage()).isEqualTo("otro");
    assertThat(e.getPath()).isEqualTo("/q");
    assertThat(e.getDetails()).containsEntry("campo", "error");
  }
}
