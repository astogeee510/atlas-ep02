package cl.duoc.atlas.infrastructure.entrypoints.rest;

import cl.duoc.atlas.domain.exception.CredencialesInvalidasException;
import cl.duoc.atlas.domain.exception.CuentaBloqueadaException;
import cl.duoc.atlas.domain.exception.DatosInvalidosException;
import cl.duoc.atlas.domain.exception.RecursoDuplicadoException;
import cl.duoc.atlas.domain.exception.RecursoNoEncontradoException;
import cl.duoc.atlas.infrastructure.entrypoints.rest.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Traduce las excepciones a respuestas HTTP claras (NFR-USAB-3). Los errores inesperados devuelven
 * un mensaje generico: el detalle tecnico solo queda en el log del servidor (NFR-SEG-6).
 */
@RestControllerAdvice
public class ApiExceptionHandler {

  private static final Logger LOG = LoggerFactory.getLogger(ApiExceptionHandler.class);

  @ExceptionHandler(RecursoNoEncontradoException.class)
  public ResponseEntity<ErrorResponse> handleNoEncontrado(
      RecursoNoEncontradoException ex, HttpServletRequest request) {
    return respuesta(HttpStatus.NOT_FOUND, ex.getMessage(), request);
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<ErrorResponse> handleRecursoInexistente(
      NoResourceFoundException ex, HttpServletRequest request) {
    return respuesta(HttpStatus.NOT_FOUND, "El recurso solicitado no existe", request);
  }

  @ExceptionHandler(RecursoDuplicadoException.class)
  public ResponseEntity<ErrorResponse> handleDuplicado(
      RecursoDuplicadoException ex, HttpServletRequest request) {
    return respuesta(HttpStatus.CONFLICT, ex.getMessage(), request);
  }

  @ExceptionHandler(DatosInvalidosException.class)
  public ResponseEntity<ErrorResponse> handleDatosInvalidos(
      DatosInvalidosException ex, HttpServletRequest request) {
    return respuesta(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
  }

  @ExceptionHandler(CredencialesInvalidasException.class)
  public ResponseEntity<ErrorResponse> handleCredenciales(
      CredencialesInvalidasException ex, HttpServletRequest request) {
    return respuesta(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
  }

  @ExceptionHandler(CuentaBloqueadaException.class)
  public ResponseEntity<ErrorResponse> handleBloqueo(
      CuentaBloqueadaException ex, HttpServletRequest request) {
    return respuesta(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage(), request);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidation(
      MethodArgumentNotValidException ex, HttpServletRequest request) {
    Map<String, String> details = new LinkedHashMap<>();
    for (FieldError error : ex.getBindingResult().getFieldErrors()) {
      details.putIfAbsent(error.getField(), error.getDefaultMessage());
    }
    ErrorResponse body =
        cuerpo(HttpStatus.BAD_REQUEST, "La solicitud contiene datos inválidos", request);
    body.setDetails(details);
    return ResponseEntity.badRequest().body(body);
  }

  @ExceptionHandler({
    HttpMessageNotReadableException.class,
    MethodArgumentTypeMismatchException.class
  })
  public ResponseEntity<ErrorResponse> handleFormato(Exception ex, HttpServletRequest request) {
    return respuesta(
        HttpStatus.BAD_REQUEST,
        "El formato de la solicitud no es válido (revise JSON, fechas yyyy-MM-dd y números)",
        request);
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ErrorResponse> handleMetodo(
      HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
    return respuesta(HttpStatus.METHOD_NOT_ALLOWED, "Método HTTP no permitido", request);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
    LOG.error("Error inesperado en {}", request.getRequestURI(), ex);
    return respuesta(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "Ocurrió un error inesperado. Intente nuevamente más tarde",
        request);
  }

  private ResponseEntity<ErrorResponse> respuesta(
      HttpStatus estado, String mensaje, HttpServletRequest request) {
    return ResponseEntity.status(estado).body(cuerpo(estado, mensaje, request));
  }

  private ErrorResponse cuerpo(HttpStatus estado, String mensaje, HttpServletRequest request) {
    return new ErrorResponse(
        estado.value(), estado.getReasonPhrase(), mensaje, request.getRequestURI());
  }
}
