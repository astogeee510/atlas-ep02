package cl.duoc.atlas.infrastructure.entrypoints.rest;

import cl.duoc.atlas.application.usecase.ClienteUseCase;
import cl.duoc.atlas.infrastructure.entrypoints.rest.dto.ClienteRequest;
import cl.duoc.atlas.infrastructure.entrypoints.rest.dto.ClienteResponse;
import cl.duoc.atlas.infrastructure.entrypoints.rest.dto.PaginaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Adaptador de entrada REST de clientes (RF-4). Escritura solo para ADMIN (SecurityConfig). */
@RestController
@RequestMapping("/api/v1/clientes")
@Tag(name = "Clientes", description = "Gestión de clientes")
public class ClienteController {

  private final ClienteUseCase clienteUseCase;

  public ClienteController(ClienteUseCase clienteUseCase) {
    this.clienteUseCase = clienteUseCase;
  }

  @GetMapping
  @Operation(summary = "Lista clientes activos con filtro por nombre y paginación")
  public PaginaResponse<ClienteResponse> listar(
      @RequestParam(defaultValue = "") String nombre,
      @RequestParam(defaultValue = "0") int pagina,
      @RequestParam(defaultValue = "10") int tamano) {
    return PaginaResponse.fromDomain(
        clienteUseCase.listar(nombre, pagina, tamano), ClienteResponse::fromDomain);
  }

  @GetMapping("/{id}")
  @Operation(summary = "Obtiene un cliente por su id")
  @ApiResponse(responseCode = "200", description = "Cliente encontrado")
  @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
  public ClienteResponse obtener(@PathVariable Long id) {
    return ClienteResponse.fromDomain(clienteUseCase.obtener(id));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Crea un cliente (solo ADMIN)")
  @ApiResponse(responseCode = "201", description = "Cliente creado")
  @ApiResponse(responseCode = "400", description = "Datos inválidos")
  @ApiResponse(responseCode = "409", description = "RUT duplicado")
  public ClienteResponse crear(@Valid @RequestBody ClienteRequest request, Principal principal) {
    return ClienteResponse.fromDomain(
        clienteUseCase.crear(request.toDomain(), principal.getName()));
  }

  @PutMapping("/{id}")
  @Operation(summary = "Actualiza un cliente (solo ADMIN)")
  @ApiResponse(responseCode = "200", description = "Cliente actualizado")
  @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
  public ClienteResponse actualizar(
      @PathVariable Long id, @Valid @RequestBody ClienteRequest request, Principal principal) {
    return ClienteResponse.fromDomain(
        clienteUseCase.actualizar(id, request.toDomain(), principal.getName()));
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Elimina lógicamente un cliente; exige confirmar=true (solo ADMIN)")
  @ApiResponse(responseCode = "204", description = "Cliente eliminado")
  @ApiResponse(responseCode = "400", description = "Falta la confirmación")
  public void eliminar(
      @PathVariable Long id,
      @RequestParam(defaultValue = "false") boolean confirmar,
      Principal principal) {
    clienteUseCase.eliminar(id, confirmar, principal.getName());
  }
}
