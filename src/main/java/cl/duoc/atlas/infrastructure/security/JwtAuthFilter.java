package cl.duoc.atlas.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Lee el header {@code Authorization: Bearer <jwt>}, valida el token y deja al usuario con su rol
 * (ROLE_ADMIN o ROLE_EDITOR) en el contexto de seguridad. Si el token falta o no es valido, la
 * peticion sigue como anonima y Spring Security responde 401 en las rutas protegidas.
 */
public class JwtAuthFilter extends OncePerRequestFilter {

  /** Prefijo del header Authorization. */
  public static final String PREFIJO = "Bearer ";

  private final JwtService jwtService;

  public JwtAuthFilter(JwtService jwtService) {
    this.jwtService = jwtService;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String cabecera = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (cabecera != null && cabecera.startsWith(PREFIJO)) {
      jwtService
          .validar(cabecera.substring(PREFIJO.length()))
          .ifPresent(
              token ->
                  SecurityContextHolder.getContext()
                      .setAuthentication(
                          new UsernamePasswordAuthenticationToken(
                              token.nombreUsuario(),
                              null,
                              List.of(new SimpleGrantedAuthority("ROLE_" + token.rol())))));
    }
    filterChain.doFilter(request, response);
  }
}
