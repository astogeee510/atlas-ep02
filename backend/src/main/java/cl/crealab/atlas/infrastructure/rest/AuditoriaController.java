package cl.crealab.atlas.infrastructure.rest;

import cl.crealab.atlas.application.AuditoriaService;
import cl.crealab.atlas.application.dto.AuditoriaResponse;
import cl.crealab.atlas.application.dto.PaginaResponse;
import cl.crealab.atlas.infrastructure.security.UsuarioAutenticado;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Consulta de auditoria (RF-6.1). Solo administradores. */
@RestController
public class AuditoriaController {

  private final AuditoriaService servicio;

  public AuditoriaController(AuditoriaService servicio) {
    this.servicio = servicio;
  }

  @GetMapping("/api/empresas/{empresaId}/auditoria")
  public PaginaResponse<AuditoriaResponse> listar(
      @AuthenticationPrincipal UsuarioAutenticado usuario,
      @PathVariable Long empresaId,
      @RequestParam(defaultValue = "0") int pagina,
      @RequestParam(defaultValue = "20") int tamano) {
    return servicio.listar(usuario.id(), empresaId, pagina, tamano);
  }
}
