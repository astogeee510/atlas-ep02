package cl.crealab.atlas.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Emite y valida los JWT de sesion (NFR-SEG-9): token de acceso de corta vigencia y token de
 * refresco. Cada token lleva un jti unico para poder revocarlo en el logout (RF-1.2).
 */
@Component
public class JwtService {

  /** Tipo de token de acceso. */
  public static final String TIPO_ACCESO = "access";

  /** Tipo de token de refresco. */
  public static final String TIPO_REFRESCO = "refresh";

  private static final String CLAIM_TIPO = "tipo";
  private static final String CLAIM_USUARIO = "usr";

  private final SecretKey clave;
  private final Duration duracionAcceso;
  private final Duration duracionRefresco;
  private final Clock clock;

  public JwtService(
      @Value("${atlas.jwt.secret}") String secretoBase64,
      @Value("${atlas.jwt.access-minutos}") long accessMinutos,
      @Value("${atlas.jwt.refresh-dias}") long refreshDias,
      Clock clock) {
    this.clave = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretoBase64));
    this.duracionAcceso = Duration.ofMinutes(accessMinutos);
    this.duracionRefresco = Duration.ofDays(refreshDias);
    this.clock = clock;
  }

  /**
   * Firma un token para el usuario.
   *
   * @param usuarioId id del usuario
   * @param nombreUsuario nombre de usuario
   * @param tipo {@link #TIPO_ACCESO} o {@link #TIPO_REFRESCO}
   * @return token firmado
   */
  public TokenEmitido emitir(Long usuarioId, String nombreUsuario, String tipo) {
    Instant ahora = clock.instant();
    Instant expira = ahora.plus(TIPO_ACCESO.equals(tipo) ? duracionAcceso : duracionRefresco);
    String jti = UUID.randomUUID().toString();
    String token =
        Jwts.builder()
            .id(jti)
            .subject(String.valueOf(usuarioId))
            .claim(CLAIM_USUARIO, nombreUsuario)
            .claim(CLAIM_TIPO, tipo)
            .issuedAt(Date.from(ahora))
            .expiration(Date.from(expira))
            .signWith(clave)
            .compact();
    return new TokenEmitido(token, jti, expira);
  }

  /**
   * Valida firma, expiracion y tipo de un token.
   *
   * @param token JWT compacto
   * @param tipoEsperado tipo que debe tener el token
   * @return datos del usuario si el token es valido
   */
  public Optional<UsuarioAutenticado> validar(String token, String tipoEsperado) {
    if (token == null || token.isBlank()) {
      return Optional.empty();
    }
    try {
      Claims claims =
          Jwts.parser()
              .verifyWith(clave)
              .clock(() -> Date.from(clock.instant()))
              .build()
              .parseSignedClaims(token)
              .getPayload();
      if (!tipoEsperado.equals(claims.get(CLAIM_TIPO, String.class))) {
        return Optional.empty();
      }
      return Optional.of(
          new UsuarioAutenticado(
              Long.valueOf(claims.getSubject()),
              claims.get(CLAIM_USUARIO, String.class),
              claims.getId(),
              claims.getExpiration().toInstant()));
    } catch (JwtException | IllegalArgumentException e) {
      return Optional.empty();
    }
  }

  public Duration getDuracionAcceso() {
    return duracionAcceso;
  }

  public Duration getDuracionRefresco() {
    return duracionRefresco;
  }
}
