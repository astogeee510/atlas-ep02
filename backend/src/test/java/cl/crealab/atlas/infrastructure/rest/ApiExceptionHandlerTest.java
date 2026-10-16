package cl.crealab.atlas.infrastructure.rest;

import static org.assertj.core.api.Assertions.assertThat;

import cl.crealab.atlas.domain.exception.AccesoDenegadoException;
import cl.crealab.atlas.domain.exception.ArchivoNoPermitidoException;
import cl.crealab.atlas.domain.exception.ConflictoException;
import cl.crealab.atlas.domain.exception.CredencialesInvalidasException;
import cl.crealab.atlas.domain.exception.DemasiadosIntentosException;
import cl.crealab.atlas.domain.exception.EnlaceExpiradoException;
import cl.crealab.atlas.domain.exception.NoEncontradoException;
import cl.crealab.atlas.domain.exception.ValidacionException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/** NFR-SEG-6 / NFR-USAB-3: errores claros y sin fuga de informacion interna. */
class ApiExceptionHandlerTest {

  private final ApiExceptionHandler handler = new ApiExceptionHandler();
  private final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/x");

  @Test
  void mapeaCadaExcepcionASuCodigo() {
    assertThat(
            handler.noEncontrado(new NoEncontradoException("a"), request).getStatusCode().value())
        .isEqualTo(404);
    assertThat(handler.conflicto(new ConflictoException("a"), request).getStatusCode().value())
        .isEqualTo(409);
    assertThat(
            handler
                .accesoDenegado(new AccesoDenegadoException("a"), request)
                .getStatusCode()
                .value())
        .isEqualTo(403);
    assertThat(
            handler
                .noAutenticado(new CredencialesInvalidasException("a"), request)
                .getStatusCode()
                .value())
        .isEqualTo(401);
    assertThat(
            handler
                .archivoNoPermitido(new ArchivoNoPermitidoException("a"), request)
                .getStatusCode()
                .value())
        .isEqualTo(415);
    assertThat(
            handler
                .enlaceExpirado(new EnlaceExpiradoException("a"), request)
                .getStatusCode()
                .value())
        .isEqualTo(410);
    assertThat(
            handler
                .demasiadosIntentos(new DemasiadosIntentosException("a"), request)
                .getStatusCode()
                .value())
        .isEqualTo(429);
    assertThat(
            handler
                .archivoGrande(new MaxUploadSizeExceededException(1), request)
                .getStatusCode()
                .value())
        .isEqualTo(413);
  }

  @Test
  void validacionDeNegocioIncluyeDetalles() {
    ResponseEntity<ErrorResponse> r =
        handler.validacionNegocio(new ValidacionException("x", List.of("a", "b")), request);
    assertThat(r.getStatusCode().value()).isEqualTo(400);
    assertThat(r.getBody().detalles()).containsExactly("a", "b");
    assertThat(r.getBody().ruta()).isEqualTo("/api/x");
  }

  @Test
  void conflictoDeBaseDeDatosNoExponeElDetalleSql() {
    ResponseEntity<ErrorResponse> r =
        handler.conflicto(
            new DataIntegrityViolationException("duplicate key ux_empresa_correo"), request);
    assertThat(r.getBody().mensaje()).doesNotContain("ux_empresa_correo");
  }

  @Test
  void errorInesperadoNoExponeLaCausa() {
    ResponseEntity<ErrorResponse> r =
        handler.generico(new IllegalStateException("password=1234 en /opt/app"), request);
    assertThat(r.getStatusCode().value()).isEqualTo(500);
    assertThat(r.getBody().mensaje()).doesNotContain("password").doesNotContain("/opt");
  }
}
