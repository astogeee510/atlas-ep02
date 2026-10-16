package cl.duoc.atlas.infrastructure.entrypoints.rest;

import cl.duoc.atlas.application.usecase.ContratoUseCase;
import cl.duoc.atlas.domain.model.Contrato;
import cl.duoc.atlas.infrastructure.entrypoints.rest.dto.ContratoRequest;
import cl.duoc.atlas.infrastructure.entrypoints.rest.dto.ContratoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptador de entrada REST de contratos (RF-5.1, RF-5.4). Los contratos se consultan a partir de
 * su cliente (NFR-USAB-4).
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Contratos", description = "Contratos de cada cliente con su estado calculado")
public class ContratoController {

  private final ContratoUseCase contratoUseCase;

  public ContratoController(ContratoUseCase contratoUseCase) {
    this.contratoUseCase = contratoUseCase;
  }

  @GetMapping("/clientes/{clienteId}/contratos")
  @Operation(summary = "Lista los contratos de un cliente con su estado")
  @ApiResponse(responseCode = "200", description = "Contratos del cliente")
  @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
  public List<ContratoResponse> listar(@PathVariable Long clienteId) {
    return contratoUseCase.listarPorCliente(clienteId).stream().map(this::respuesta).toList();
  }

  @PostMapping("/clientes/{clienteId}/contratos")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Crea un contrato para el cliente (solo ADMIN)")
  @ApiResponse(responseCode = "201", description = "Contrato creado")
  @ApiResponse(responseCode = "400", description = "Datos o fechas inválidas")
  @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
  public ContratoResponse crear(
      @PathVariable Long clienteId,
      @Valid @RequestBody ContratoRequest request,
      Principal principal) {
    return respuesta(contratoUseCase.crear(clienteId, request.toDomain(), principal.getName()));
  }

  @GetMapping("/contratos/{id}")
  @Operation(summary = "Obtiene un contrato con su estado")
  @ApiResponse(responseCode = "200", description = "Contrato encontrado")
  @ApiResponse(responseCode = "404", description = "Contrato no encontrado")
  public ContratoResponse obtener(@PathVariable Long id) {
    return respuesta(contratoUseCase.obtener(id));
  }

  @PutMapping("/contratos/{id}")
  @Operation(summary = "Actualiza un contrato (solo ADMIN)")
  @ApiResponse(responseCode = "200", description = "Contrato actualizado")
  @ApiResponse(responseCode = "404", description = "Contrato no encontrado")
  public ContratoResponse actualizar(
      @PathVariable Long id, @Valid @RequestBody ContratoRequest request, Principal principal) {
    return respuesta(contratoUseCase.actualizar(id, request.toDomain(), principal.getName()));
  }

  @DeleteMapping("/contratos/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Elimina un contrato (solo ADMIN)")
  @ApiResponse(responseCode = "204", description = "Contrato eliminado")
  @ApiResponse(responseCode = "404", description = "Contrato no encontrado")
  public void eliminar(@PathVariable Long id, Principal principal) {
    contratoUseCase.eliminar(id, principal.getName());
  }

  private ContratoResponse respuesta(Contrato contrato) {
    return ContratoResponse.fromDomain(contrato, contratoUseCase.estado(contrato));
  }
}
