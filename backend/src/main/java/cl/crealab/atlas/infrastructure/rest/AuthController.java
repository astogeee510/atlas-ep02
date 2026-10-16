package cl.crealab.atlas.infrastructure.rest;

import cl.crealab.atlas.application.AuthService;
import cl.crealab.atlas.application.dto.LoginRequest;
import cl.crealab.atlas.application.dto.PerfilResponse;
import cl.crealab.atlas.application.dto.RegistroRequest;
import cl.crealab.atlas.application.dto.ResultadoLogin;
import cl.crealab.atlas.application.dto.SesionResponse;
import cl.crealab.atlas.application.dto.UsuarioResponse;
import cl.crealab.atlas.domain.exception.CredencialesInvalidasException;
import cl.crealab.atlas.infrastructure.security.TokenEmitido;
import cl.crealab.atlas.infrastructure.security.UsuarioAutenticado;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.Duration;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Autenticacion y sesion (RF-1, RF-2.1). */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

  /** Nombre de la cookie HttpOnly con el token de refresco. */
  public static final String COOKIE_REFRESCO = "atlas_refresh";

  private static final String RUTA_COOKIE = "/api/auth";

  private final AuthService authService;
  private final boolean cookieSegura;

  public AuthController(
      AuthService authService, @Value("${atlas.cookie.secure}") boolean cookieSegura) {
    this.authService = authService;
    this.cookieSegura = cookieSegura;
  }

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public UsuarioResponse registrar(@Valid @RequestBody RegistroRequest solicitud) {
    return authService.registrar(solicitud);
  }

  @PostMapping("/login")
  public ResponseEntity<SesionResponse> login(
      @Valid @RequestBody LoginRequest solicitud, HttpServletRequest request) {
    ResultadoLogin resultado = authService.login(solicitud, request.getRemoteAddr());
    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, cookieRefresco(resultado.refresco()).toString())
        .body(resultado.sesion());
  }

  @PostMapping("/refresh")
  public SesionResponse refrescar(
      @CookieValue(name = COOKIE_REFRESCO, required = false) String tokenRefresco) {
    if (tokenRefresco == null) {
      throw new CredencialesInvalidasException("No hay sesion activa");
    }
    return authService.refrescar(tokenRefresco);
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(
      @AuthenticationPrincipal UsuarioAutenticado usuario,
      @CookieValue(name = COOKIE_REFRESCO, required = false) String tokenRefresco) {
    authService.logout(usuario, tokenRefresco);
    return ResponseEntity.noContent()
        .header(HttpHeaders.SET_COOKIE, cookieVacia().toString())
        .build();
  }

  @GetMapping("/me")
  public PerfilResponse perfil(@AuthenticationPrincipal UsuarioAutenticado usuario) {
    return authService.perfil(usuario.id());
  }

  private ResponseCookie cookieRefresco(TokenEmitido refresco) {
    return ResponseCookie.from(COOKIE_REFRESCO, refresco.token())
        .httpOnly(true)
        .secure(cookieSegura)
        .sameSite("Strict")
        .path(RUTA_COOKIE)
        .maxAge(Duration.between(Instant.now(), refresco.expiraEn()))
        .build();
  }

  private ResponseCookie cookieVacia() {
    return ResponseCookie.from(COOKIE_REFRESCO, "")
        .httpOnly(true)
        .secure(cookieSegura)
        .sameSite("Strict")
        .path(RUTA_COOKIE)
        .maxAge(0)
        .build();
  }
}
