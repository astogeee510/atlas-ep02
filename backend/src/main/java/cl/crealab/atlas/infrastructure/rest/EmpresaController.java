package cl.crealab.atlas.infrastructure.rest;

import cl.crealab.atlas.application.AuthService;
import cl.crealab.atlas.application.EmpresaService;
import cl.crealab.atlas.application.dto.DashboardResponse;
import cl.crealab.atlas.application.dto.EmpresaRequest;
import cl.crealab.atlas.application.dto.EmpresaResponse;
import cl.crealab.atlas.application.dto.MembresiaResponse;
import cl.crealab.atlas.infrastructure.security.UsuarioAutenticado;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Empresas del usuario y dashboard (RF-2.2, RF-2.3). */
@RestController
@RequestMapping("/api/empresas")
public class EmpresaController {

  private final EmpresaService empresaService;
  private final AuthService authService;

  public EmpresaController(EmpresaService empresaService, AuthService authService) {
    this.empresaService = empresaService;
    this.authService = authService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public EmpresaResponse crear(
      @AuthenticationPrincipal UsuarioAutenticado usuario,
      @Valid @RequestBody EmpresaRequest solicitud) {
    return empresaService.crear(usuario.id(), solicitud);
  }

  @GetMapping
  public List<MembresiaResponse> misEmpresas(@AuthenticationPrincipal UsuarioAutenticado usuario) {
    return authService.empresasDe(usuario.id());
  }

  @GetMapping("/{empresaId}")
  public EmpresaResponse obtener(
      @AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable Long empresaId) {
    return empresaService.obtener(usuario.id(), empresaId);
  }

  @GetMapping("/{empresaId}/dashboard")
  public DashboardResponse dashboard(
      @AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable Long empresaId) {
    return empresaService.dashboard(usuario.id(), empresaId);
  }
}
