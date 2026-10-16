package cl.duoc.atlas.infrastructure.entrypoints.rest;

import cl.duoc.atlas.application.usecase.AuthUseCase;
import cl.duoc.atlas.infrastructure.entrypoints.rest.dto.LoginRequest;
import cl.duoc.atlas.infrastructure.entrypoints.rest.dto.TokenResponse;
import cl.duoc.atlas.infrastructure.security.JwtAuthFilter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Adaptador de entrada REST de autenticacion (RF-1). */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticación", description = "Inicio y cierre de sesión con JWT")
public class AuthController {

  private final AuthUseCase authUseCase;

  public AuthController(AuthUseCase authUseCase) {
    this.authUseCase = authUseCase;
  }

  @PostMapping("/login")
  @Operation(summary = "Inicia sesión y entrega un JWT de acceso")
  @ApiResponse(responseCode = "200", description = "Login correcto")
  @ApiResponse(responseCode = "401", description = "Usuario o contraseña incorrectos")
  @ApiResponse(responseCode = "429", description = "Cuenta bloqueada por intentos fallidos")
  public TokenResponse login(@Valid @RequestBody LoginRequest request) {
    return TokenResponse.fromDomain(
        authUseCase.login(request.nombreUsuario(), request.contrasena()));
  }

  @PostMapping("/logout")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Cierra la sesión revocando el token actual")
  @ApiResponse(responseCode = "204", description = "Sesión cerrada")
  @ApiResponse(responseCode = "401", description = "Token ausente, inválido o ya revocado")
  public void logout(
      @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, Principal principal) {
    String token = authorization.substring(JwtAuthFilter.PREFIJO.length());
    authUseCase.logout(token, principal.getName());
  }
}
