package cl.duoc.atlas.infrastructure.security;

import cl.duoc.atlas.infrastructure.entrypoints.rest.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

/**
 * Respuestas JSON claras (NFR-USAB-3) cuando falta autenticacion (401) o el rol no tiene permiso
 * (403, por ejemplo un EDITOR que intenta escribir: RF-3.2, RB-4).
 */
public class RespuestaErrorSeguridad implements AuthenticationEntryPoint, AccessDeniedHandler {

  private final ObjectMapper objectMapper;

  public RespuestaErrorSeguridad(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException {
    escribir(
        request,
        response,
        HttpStatus.UNAUTHORIZED,
        "Debe iniciar sesión y enviar un token válido en el header Authorization");
  }

  @Override
  public void handle(
      HttpServletRequest request,
      HttpServletResponse response,
      AccessDeniedException accessDeniedException)
      throws IOException {
    escribir(
        request, response, HttpStatus.FORBIDDEN, "Su rol no tiene permiso para esta operación");
  }

  private void escribir(
      HttpServletRequest request, HttpServletResponse response, HttpStatus estado, String mensaje)
      throws IOException {
    ErrorResponse body =
        new ErrorResponse(
            estado.value(), estado.getReasonPhrase(), mensaje, request.getRequestURI());
    response.setStatus(estado.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");
    objectMapper.writeValue(response.getOutputStream(), body);
  }
}
