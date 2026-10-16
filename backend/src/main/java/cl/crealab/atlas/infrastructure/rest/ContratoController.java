package cl.crealab.atlas.infrastructure.rest;

import cl.crealab.atlas.application.ContratoService;
import cl.crealab.atlas.application.dto.ContratoRequest;
import cl.crealab.atlas.application.dto.ContratoResponse;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Gestion de contratos (RF-5.1, RF-5.4). */
@RestController
public class ContratoController {

  private final ContratoService servicio;

  public ContratoController(ContratoService servicio) {
    this.servicio = servicio;
  }

  @GetMapping("/api/clientes/{clienteId}/contratos")
  public List<ContratoResponse> listarPorCliente(
      @AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable Long clienteId) {
    return servicio.listarPorCliente(usuario.id(), clienteId);
  }

  @PostMapping("/api/contratos")
  @ResponseStatus(HttpStatus.CREATED)
  public ContratoResponse crear(
      @AuthenticationPrincipal UsuarioAutenticado usuario,
      @Valid @RequestBody ContratoRequest solicitud) {
    return servicio.crear(usuario.id(), solicitud);
  }

  @GetMapping("/api/contratos/{contratoId}")
  public ContratoResponse obtener(
      @AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable Long contratoId) {
    return servicio.obtener(usuario.id(), contratoId);
  }

  @PutMapping("/api/contratos/{contratoId}")
  public ContratoResponse actualizar(
      @AuthenticationPrincipal UsuarioAutenticado usuario,
      @PathVariable Long contratoId,
      @Valid @RequestBody ContratoRequest solicitud) {
    return servicio.actualizar(usuario.id(), contratoId, solicitud);
  }

  @DeleteMapping("/api/contratos/{contratoId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void eliminar(
      @AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable Long contratoId) {
    servicio.eliminar(usuario.id(), contratoId);
  }
}
