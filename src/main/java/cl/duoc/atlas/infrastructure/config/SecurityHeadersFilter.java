package cl.duoc.atlas.infrastructure.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Agrega cabeceras de seguridad HTTP a todas las respuestas, para mitigar clickjacking, sniffing de
 * contenido e inyeccion de scripts (NFR-SEG-5). La politica CSP solo permite recursos propios y el
 * script de Redoc desde su CDN oficial. Se ejecuta antes que Spring Security para que tambien las
 * respuestas 401/403 lleven las cabeceras.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SecurityHeadersFilter extends OncePerRequestFilter {

  /** Politica de seguridad de contenido aplicada a todas las respuestas. */
  public static final String CSP =
      "default-src 'self'; script-src 'self' https://cdn.redoc.ly; "
          + "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; "
          + "font-src 'self' https://fonts.gstatic.com; img-src 'self' data: https://cdn.redoc.ly; "
          + "worker-src 'self' blob:; connect-src 'self'; frame-ancestors 'none'; "
          + "form-action 'self'; base-uri 'self'";

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    response.setHeader("X-Content-Type-Options", "nosniff");
    response.setHeader("X-Frame-Options", "DENY");
    response.setHeader("Content-Security-Policy", CSP);
    response.setHeader("Referrer-Policy", "no-referrer");
    response.setHeader("Permissions-Policy", "geolocation=(), camera=(), microphone=()");
    response.setHeader("Cross-Origin-Opener-Policy", "same-origin");
    response.setHeader("Cross-Origin-Resource-Policy", "same-origin");
    filterChain.doFilter(request, response);
  }
}
