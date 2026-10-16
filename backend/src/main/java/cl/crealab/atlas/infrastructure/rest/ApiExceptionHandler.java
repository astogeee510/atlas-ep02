package cl.crealab.atlas.infrastructure.rest;

import cl.crealab.atlas.domain.exception.AccesoDenegadoException;
import cl.crealab.atlas.domain.exception.ArchivoNoPermitidoException;
import cl.crealab.atlas.domain.exception.ConflictoException;
import cl.crealab.atlas.domain.exception.CredencialesInvalidasException;
import cl.crealab.atlas.domain.exception.DemasiadosIntentosException;
import cl.crealab.atlas.domain.exception.EnlaceExpiradoException;
import cl.crealab.atlas.domain.exception.NoEncontradoException;
import cl.crealab.atlas.domain.exception.ValidacionException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/** Traduce las excepciones a respuestas HTTP claras y sin fuga de informacion interna. */
@RestControllerAdvice
public class ApiExceptionHandler {

  private static final Logger LOG = LoggerFactory.getLogger(ApiExceptionHandler.class);

  @ExceptionHandler(NoEncontradoException.class)
  public ResponseEntity<ErrorResponse> noEncontrado(RuntimeException ex, HttpServletRequest req) {
    return responder(HttpStatus.NOT_FOUND, ex.getMessage(), List.of(), req);
  }

  @ExceptionHandler({ConflictoException.class, DataIntegrityViolationException.class})
  public ResponseEntity<ErrorResponse> conflicto(RuntimeException ex, HttpServletRequest req) {
    String mensaje =
        ex instanceof ConflictoException
            ? ex.getMessage()
            : "La operacion entra en conflicto con datos existentes";
    return responder(HttpStatus.CONFLICT, mensaje, List.of(), req);
  }

  @ExceptionHandler(AccesoDenegadoException.class)
  public ResponseEntity<ErrorResponse> accesoDenegado(RuntimeException ex, HttpServletRequest req) {
    return responder(HttpStatus.FORBIDDEN, ex.getMessage(), List.of(), req);
  }

  @ExceptionHandler(CredencialesInvalidasException.class)
  public ResponseEntity<ErrorResponse> noAutenticado(RuntimeException ex, HttpServletRequest req) {
    return responder(HttpStatus.UNAUTHORIZED, ex.getMessage(), List.of(), req);
  }

  @ExceptionHandler(ArchivoNoPermitidoException.class)
  public ResponseEntity<ErrorResponse> archivoNoPermitido(
      RuntimeException ex, HttpServletRequest req) {
    return responder(HttpStatus.UNSUPPORTED_MEDIA_TYPE, ex.getMessage(), List.of(), req);
  }

  @ExceptionHandler(EnlaceExpiradoException.class)
  public ResponseEntity<ErrorResponse> enlaceExpirado(RuntimeException ex, HttpServletRequest req) {
    return responder(HttpStatus.GONE, ex.getMessage(), List.of(), req);
  }

  @ExceptionHandler(DemasiadosIntentosException.class)
  public ResponseEntity<ErrorResponse> demasiadosIntentos(
      RuntimeException ex, HttpServletRequest req) {
    return responder(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage(), List.of(), req);
  }

  @ExceptionHandler(ValidacionException.class)
  public ResponseEntity<ErrorResponse> validacionNegocio(
      ValidacionException ex, HttpServletRequest req) {
    return responder(HttpStatus.BAD_REQUEST, ex.getMessage(), ex.getErrores(), req);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> validacionEntrada(
      MethodArgumentNotValidException ex, HttpServletRequest req) {
    List<String> detalles =
        ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .sorted()
            .toList();
    return responder(
        HttpStatus.BAD_REQUEST, "La solicitud contiene datos invalidos", detalles, req);
  }

  @ExceptionHandler({
    HttpMessageNotReadableException.class,
    MissingServletRequestParameterException.class,
    MissingServletRequestPartException.class,
    MethodArgumentTypeMismatchException.class
  })
  public ResponseEntity<ErrorResponse> solicitudMalFormada(Exception ex, HttpServletRequest req) {
    return responder(HttpStatus.BAD_REQUEST, "La solicitud esta mal formada", List.of(), req);
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ResponseEntity<ErrorResponse> archivoGrande(Exception ex, HttpServletRequest req) {
    return responder(
        HttpStatus.PAYLOAD_TOO_LARGE,
        "El archivo supera el tamano maximo de 10 MB",
        List.of(),
        req);
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<ErrorResponse> rutaInexistente(Exception ex, HttpServletRequest req) {
    return responder(HttpStatus.NOT_FOUND, "Recurso no encontrado", List.of(), req);
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ErrorResponse> metodoNoPermitido(Exception ex, HttpServletRequest req) {
    return responder(HttpStatus.METHOD_NOT_ALLOWED, "Metodo no permitido", List.of(), req);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> generico(Exception ex, HttpServletRequest req) {
    LOG.error("Error no controlado en {}", req.getRequestURI(), ex);
    return responder(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "Ocurrio un error inesperado. Intente nuevamente.",
        List.of(),
        req);
  }

  private static ResponseEntity<ErrorResponse> responder(
      HttpStatus status, String mensaje, List<String> detalles, HttpServletRequest req) {
    return ResponseEntity.status(status)
        .body(
            new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                mensaje,
                req.getRequestURI(),
                detalles,
                Instant.now()));
  }
}
