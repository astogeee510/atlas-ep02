package cl.duoc.atlas.application.usecase;

import cl.duoc.atlas.domain.exception.CredencialesInvalidasException;
import cl.duoc.atlas.domain.exception.CuentaBloqueadaException;
import cl.duoc.atlas.domain.gateway.TokenGateway;
import cl.duoc.atlas.domain.gateway.UsuarioGateway;
import cl.duoc.atlas.domain.model.Sesion;
import cl.duoc.atlas.domain.model.Usuario;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Casos de uso de autenticacion (RF-1).
 *
 * <ul>
 *   <li>Login con nombre de usuario y contrasena verificada con bcrypt (NFR-SEG-2).
 *   <li>Emision de JWT (NFR-SEG-9) y revocacion en el logout (RF-1.2).
 *   <li>Bloqueo temporal tras {@value #MAX_INTENTOS} intentos fallidos (fuerza bruta).
 * </ul>
 */
@Service
public class AuthUseCase {

  /** Intentos fallidos permitidos antes del bloqueo. */
  public static final int MAX_INTENTOS = 5;

  /** Duracion del bloqueo. */
  public static final Duration BLOQUEO = Duration.ofMinutes(15);

  private static final Logger AUDITORIA = LoggerFactory.getLogger("AUDITORIA");

  private final UsuarioGateway usuarioGateway;
  private final PasswordEncoder passwordEncoder;
  private final TokenGateway tokenGateway;
  private final Clock clock;
  private final Map<String, Intentos> intentos = new ConcurrentHashMap<>();
  private final String hashFicticio;

  public AuthUseCase(
      UsuarioGateway usuarioGateway,
      PasswordEncoder passwordEncoder,
      TokenGateway tokenGateway,
      Clock clock) {
    this.usuarioGateway = usuarioGateway;
    this.passwordEncoder = passwordEncoder;
    this.tokenGateway = tokenGateway;
    this.clock = clock;
    // Se compara contra un hash ficticio cuando el usuario no existe, para que la respuesta tarde
    // lo mismo y no permita descubrir que usuarios existen.
    this.hashFicticio = passwordEncoder.encode("usuario-inexistente");
  }

  /**
   * Inicia sesion.
   *
   * @param nombreUsuario nombre de usuario
   * @param contrasena contrasena en claro (solo se compara con el hash)
   * @return sesion con el JWT de acceso
   */
  public Sesion login(String nombreUsuario, String contrasena) {
    String clave = nombreUsuario.trim().toLowerCase(Locale.ROOT);
    Instant ahora = clock.instant();
    Intentos previos = intentos.get(clave);
    if (previos != null && previos.estaBloqueado(ahora)) {
      AUDITORIA.warn("accion=LOGIN_BLOQUEADO usuario={}", sanitizar(clave));
      throw new CuentaBloqueadaException(BLOQUEO.toMinutes());
    }
    Optional<Usuario> usuario = usuarioGateway.findByNombreUsuario(clave).filter(Usuario::isActivo);
    String hash = usuario.map(Usuario::getHashContrasena).orElse(hashFicticio);
    boolean coincide = passwordEncoder.matches(contrasena, hash);
    if (usuario.isEmpty() || !coincide) {
      registrarFallo(clave, ahora);
      AUDITORIA.warn("accion=LOGIN_FALLIDO usuario={}", sanitizar(clave));
      throw new CredencialesInvalidasException();
    }
    intentos.remove(clave);
    AUDITORIA.info("accion=LOGIN usuario={}", clave);
    return tokenGateway.emitir(usuario.get());
  }

  /**
   * Cierra la sesion revocando el token (RF-1.2).
   *
   * @param token JWT de acceso
   * @param nombreUsuario usuario autenticado
   */
  public void logout(String token, String nombreUsuario) {
    tokenGateway.revocar(token);
    AUDITORIA.info("accion=LOGOUT usuario={}", nombreUsuario);
  }

  private void registrarFallo(String clave, Instant ahora) {
    intentos.compute(
        clave,
        (k, previo) -> {
          int fallidos = previo == null || previo.bloqueoVencido(ahora) ? 1 : previo.fallidos() + 1;
          Instant hasta = fallidos >= MAX_INTENTOS ? ahora.plus(BLOQUEO) : null;
          return new Intentos(fallidos, hasta);
        });
  }

  /** Evita inyeccion de lineas falsas en el log (CWE-117). */
  private static String sanitizar(String valor) {
    return valor.replaceAll("[\\r\\n\\t]", "_");
  }

  /** Intentos fallidos de un usuario y, si corresponde, hasta cuando esta bloqueado. */
  private record Intentos(int fallidos, Instant bloqueadoHasta) {

    boolean estaBloqueado(Instant ahora) {
      return bloqueadoHasta != null && ahora.isBefore(bloqueadoHasta);
    }

    boolean bloqueoVencido(Instant ahora) {
      return bloqueadoHasta != null && !ahora.isBefore(bloqueadoHasta);
    }
  }
}
