package cl.crealab.atlas.infrastructure.rest;

import cl.crealab.atlas.application.ClienteService;
import cl.crealab.atlas.application.dto.ClienteRequest;
import cl.crealab.atlas.application.dto.ClienteResponse;
import cl.crealab.atlas.application.dto.PaginaResponse;
import cl.crealab.atlas.infrastructure.security.UsuarioAutenticado;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Gestion de clientes (RF-4). */
@RestController
public class ClienteController {

  private final ClienteService servicio;

  public ClienteController(ClienteService servicio) {
    this.servicio = servicio;
  }

  @GetMapping("/api/empresas/{empresaId}/clientes")
  public PaginaResponse<ClienteResponse> listar(
      @AuthenticationPrincipal UsuarioAutenticado usuario,
      @PathVariable Long empresaId,
      @RequestParam(required = false) String nombre,
      @RequestParam(defaultValue = "0") int pagina,
      @RequestParam(defaultValue = "10") int tamano) {
    return servicio.listar(usuario.id(), empresaId, nombre, pagina, tamano);
  }

  @PostMapping("/api/empresas/{empresaId}/clientes")
  @ResponseStatus(HttpStatus.CREATED)
  public ClienteResponse crear(
      @AuthenticationPrincipal UsuarioAutenticado usuario,
      @PathVariable Long empresaId,
      @Valid @RequestBody ClienteRequest solicitud) {
    return servicio.crear(usuario.id(), empresaId, solicitud);
  }

  @GetMapping("/api/clientes/{clienteId}")
  public ClienteResponse obtener(
      @AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable Long clienteId) {
    return servicio.obtener(usuario.id(), clienteId);
  }

  @PutMapping("/api/clientes/{clienteId}")
  public ClienteResponse actualizar(
      @AuthenticationPrincipal UsuarioAutenticado usuario,
      @PathVariable Long clienteId,
      @Valid @RequestBody ClienteRequest solicitud) {
    return servicio.actualizar(usuario.id(), clienteId, solicitud);
  }

  @DeleteMapping("/api/clientes/{clienteId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void eliminar(
      @AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable Long clienteId) {
    servicio.eliminar(usuario.id(), clienteId);
  }
}
