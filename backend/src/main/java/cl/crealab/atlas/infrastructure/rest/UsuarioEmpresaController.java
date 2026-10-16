package cl.crealab.atlas.infrastructure.rest;

import cl.crealab.atlas.application.UsuarioEmpresaService;
import cl.crealab.atlas.application.dto.NuevoUsuarioRequest;
import cl.crealab.atlas.application.dto.RolRequest;
import cl.crealab.atlas.application.dto.UsuarioEmpresaResponse;
import cl.crealab.atlas.infrastructure.security.UsuarioAutenticado;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Usuarios de la empresa y roles (RF-3). */
@RestController
@RequestMapping("/api/empresas/{empresaId}/usuarios")
public class UsuarioEmpresaController {

  private final UsuarioEmpresaService servicio;

  public UsuarioEmpresaController(UsuarioEmpresaService servicio) {
    this.servicio = servicio;
  }

  @GetMapping
  public List<UsuarioEmpresaResponse> listar(
      @AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable Long empresaId) {
    return servicio.listar(usuario.id(), empresaId);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public UsuarioEmpresaResponse agregar(
      @AuthenticationPrincipal UsuarioAutenticado usuario,
      @PathVariable Long empresaId,
      @Valid @RequestBody NuevoUsuarioRequest solicitud) {
    return servicio.agregar(usuario.id(), empresaId, solicitud);
  }

  @PutMapping("/{usuarioId}")
  public UsuarioEmpresaResponse cambiarRol(
      @AuthenticationPrincipal UsuarioAutenticado usuario,
      @PathVariable Long empresaId,
      @PathVariable Long usuarioId,
      @Valid @RequestBody RolRequest solicitud) {
    return servicio.cambiarRol(usuario.id(), empresaId, usuarioId, solicitud.rol());
  }

  @DeleteMapping("/{usuarioId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void eliminar(
      @AuthenticationPrincipal UsuarioAutenticado usuario,
      @PathVariable Long empresaId,
      @PathVariable Long usuarioId) {
    servicio.eliminar(usuario.id(), empresaId, usuarioId);
  }
}
