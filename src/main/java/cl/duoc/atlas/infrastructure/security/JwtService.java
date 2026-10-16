package cl.duoc.atlas.infrastructure.security;

import cl.duoc.atlas.domain.gateway.TokenGateway;
import cl.duoc.atlas.domain.model.Sesion;
import cl.duoc.atlas.domain.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Emite, valida y revoca los JWT de acceso (RF-1, NFR-SEG-9).
 *
 * <ul>
 *   <li>Firma HMAC-SHA256 con un secreto de al menos 256 bits (variable ATLAS_JWT_SECRET).
 *   <li>Vigencia corta (15 minutos por defecto) y un jti unico por token.
 *   <li>Logout: el jti queda en una lista de revocacion hasta que el token expire.
 *   <li>Clase final: el constructor valida el secreto y puede fallar (SpotBugs
 *       CT_CONSTRUCTOR_THROW).
 * </ul>
 */
@Component
public final class JwtService implements TokenGateway {

  private static final Logger LOG = LoggerFactory.getLogger(JwtService.class);
  private static final String CLAIM_ROL = "rol";

  private final SecretKey clave;
  private final Duration duracion;
  private final Clock clock;
  private final Map<String, Instant> revocados = new ConcurrentHashMap<>();

  public JwtService(
      @Value("${atlas.jwt.secret:}") String secretoBase64,
      @Value("${atlas.jwt.minutos:15}") long minutos,
      Clock clock) {
    if (secretoBase64 == null || secretoBase64.isBlank()) {
      LOG.warn(
          "ATLAS_JWT_SECRET no definido: se genera una clave aleatoria "
              + "(los tokens se invalidan al reiniciar)");
      this.clave = Jwts.SIG.HS256.key().build();
    } else {
      this.clave = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretoBase64));
    }
    this.duracion = Duration.ofMinutes(minutos);
    this.clock = clock;
  }

  @Override
  public Sesion emitir(Usuario usuario) {
    Instant ahora = clock.instant();
    Instant expira = ahora.plus(duracion);
    String token =
        Jwts.builder()
            .id(UUID.randomUUID().toString())
            .subject(usuario.getNombreUsuario())
            .claim(CLAIM_ROL, usuario.getRol().name())
            .issuedAt(Date.from(ahora))
            .expiration(Date.from(expira))
            .signWith(clave)
            .compact();
    return new Sesion(token, expira, usuario.getNombreUsuario(), usuario.getRol());
  }

  /**
   * Valida firma, expiracion y que el token no este revocado.
   *
   * @param token JWT compacto
   * @return datos del token si es valido
   */
  public Optional<TokenValido> validar(String token) {
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
      if (revocados.containsKey(claims.getId())) {
        return Optional.empty();
      }
      return Optional.of(
          new TokenValido(
              claims.getSubject(),
              claims.get(CLAIM_ROL, String.class),
              claims.getId(),
              claims.getExpiration().toInstant()));
    } catch (JwtException | IllegalArgumentException e) {
      return Optional.empty();
    }
  }

  @Override
  public void revocar(String token) {
    Instant ahora = clock.instant();
    revocados.values().removeIf(expira -> expira.isBefore(ahora));
    validar(token).ifPresent(valido -> revocados.put(valido.jti(), valido.expiraEn()));
  }

  /**
   * Indica si un jti fue revocado.
   *
   * @param jti identificador del token
   * @return true si esta en la lista de revocacion
   */
  public boolean estaRevocado(String jti) {
    return revocados.containsKey(jti);
  }

  public Duration getDuracion() {
    return duracion;
  }
}
