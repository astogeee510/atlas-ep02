package cl.duoc.atlas.application.usecase;

import cl.duoc.atlas.domain.exception.DatosInvalidosException;
import cl.duoc.atlas.domain.exception.RecursoDuplicadoException;
import cl.duoc.atlas.domain.exception.RecursoNoEncontradoException;
import cl.duoc.atlas.domain.gateway.ClienteGateway;
import cl.duoc.atlas.domain.model.Cliente;
import cl.duoc.atlas.domain.model.Pagina;
import cl.duoc.atlas.domain.model.Rut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Casos de uso de clientes (RF-4). Orquesta las reglas de negocio y depende solo del puerto
 * ClienteGateway. Cada accion critica queda en el log de auditoria (RF-6.1) sin datos personales.
 */
@Service
public class ClienteUseCase {

  /** Tamano maximo de pagina permitido. */
  public static final int TAMANO_MAXIMO = 50;

  private static final Logger AUDITORIA = LoggerFactory.getLogger("AUDITORIA");
  private static final String RECURSO = "cliente";

  private final ClienteGateway clienteGateway;

  public ClienteUseCase(ClienteGateway clienteGateway) {
    this.clienteGateway = clienteGateway;
  }

  /**
   * Lista los clientes activos con filtro por nombre y paginacion (RF-4.2).
   *
   * @param nombre texto a buscar en el nombre (puede ser null)
   * @param pagina pagina desde 0
   * @param tamano elementos por pagina (1 a {@value #TAMANO_MAXIMO})
   * @return pagina de clientes
   */
  public Pagina<Cliente> listar(String nombre, int pagina, int tamano) {
    if (pagina < 0 || tamano < 1 || tamano > TAMANO_MAXIMO) {
      throw new DatosInvalidosException(
          "La página debe ser >= 0 y el tamaño entre 1 y " + TAMANO_MAXIMO);
    }
    String filtro = nombre == null ? "" : nombre.trim();
    return clienteGateway.buscarActivos(filtro, pagina, tamano);
  }

  /**
   * Obtiene un cliente activo.
   *
   * @param id id del cliente
   * @return el cliente
   */
  public Cliente obtener(Long id) {
    return clienteGateway
        .findById(id)
        .filter(Cliente::isActivo)
        .orElseThrow(() -> new RecursoNoEncontradoException(RECURSO, id));
  }

  /**
   * Crea un cliente validando el RUT y que no este repetido (RF-4.1, RF-4.3).
   *
   * @param cliente datos del cliente
   * @param usuario usuario que ejecuta la accion (auditoria)
   * @return cliente creado
   */
  public Cliente crear(Cliente cliente, String usuario) {
    String rut = validarRut(cliente.getRut());
    clienteGateway
        .findByRut(rut)
        .ifPresent(
            existente -> {
              throw new RecursoDuplicadoException("Ya existe un cliente con el RUT " + rut);
            });
    cliente.setId(null);
    cliente.setRut(rut);
    cliente.setActivo(true);
    Cliente creado = clienteGateway.save(cliente);
    AUDITORIA.info("accion=CREAR_CLIENTE usuario={} clienteId={}", usuario, creado.getId());
    return creado;
  }

  /**
   * Actualiza un cliente activo (RF-4.2).
   *
   * @param id id del cliente
   * @param cambios nuevos datos
   * @param usuario usuario que ejecuta la accion (auditoria)
   * @return cliente actualizado
   */
  public Cliente actualizar(Long id, Cliente cambios, String usuario) {
    Cliente actual = obtener(id);
    String rut = validarRut(cambios.getRut());
    clienteGateway
        .findByRut(rut)
        .filter(existente -> !existente.getId().equals(id))
        .ifPresent(
            existente -> {
              throw new RecursoDuplicadoException("Ya existe un cliente con el RUT " + rut);
            });
    actual.setRut(rut);
    actual.setNombre(cambios.getNombre());
    actual.setDescripcion(cambios.getDescripcion());
    actual.setDireccion(cambios.getDireccion());
    actual.setEmail(cambios.getEmail());
    actual.setTelefono(cambios.getTelefono());
    Cliente guardado = clienteGateway.save(actual);
    AUDITORIA.info("accion=EDITAR_CLIENTE usuario={} clienteId={}", usuario, id);
    return guardado;
  }

  /**
   * Elimina logicamente un cliente. Exige confirmacion explicita (criterio de aceptacion RF-4).
   *
   * @param id id del cliente
   * @param confirmado true si el usuario confirmo la eliminacion
   * @param usuario usuario que ejecuta la accion (auditoria)
   */
  public void eliminar(Long id, boolean confirmado, String usuario) {
    if (!confirmado) {
      throw new DatosInvalidosException(
          "Debe confirmar la eliminación enviando el parámetro confirmar=true");
    }
    Cliente actual = obtener(id);
    actual.setActivo(false);
    clienteGateway.save(actual);
    AUDITORIA.info("accion=ELIMINAR_CLIENTE usuario={} clienteId={}", usuario, id);
  }

  private String validarRut(String rut) {
    if (!Rut.esValido(rut)) {
      throw new DatosInvalidosException("El RUT no es válido (dígito verificador incorrecto)");
    }
    return Rut.normalizar(rut);
  }
}
