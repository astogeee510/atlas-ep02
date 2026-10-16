package cl.crealab.atlas.infrastructure.security;

import cl.crealab.atlas.infrastructure.persistence.TokenRevocadoRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Lee el header {@code Authorization: Bearer <jwt>}, valida el token de acceso y que no este
 * revocado, y deja al usuario autenticado en el contexto de seguridad.
 */
public class JwtAuthFilter extends OncePerRequestFilter {

  private static final String PREFIJO = "Bearer ";

  private final JwtService jwtService;
  private final TokenRevocadoRepository revocados;

  public JwtAuthFilter(JwtService jwtService, TokenRevocadoRepository revocados) {
    this.jwtService = jwtService;
    this.revocados = revocados;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String cabecera = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (cabecera != null && cabecera.startsWith(PREFIJO)) {
      jwtService
          .validar(cabecera.substring(PREFIJO.length()), JwtService.TIPO_ACCESO)
          .filter(usuario -> !revocados.existsById(usuario.jti()))
          .ifPresent(
              usuario ->
                  SecurityContextHolder.getContext()
                      .setAuthentication(
                          new UsernamePasswordAuthenticationToken(usuario, null, List.of())));
    }
    filterChain.doFilter(request, response);
  }
}
