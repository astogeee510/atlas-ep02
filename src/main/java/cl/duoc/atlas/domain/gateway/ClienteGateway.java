package cl.duoc.atlas.domain.gateway;

import cl.duoc.atlas.domain.model.Cliente;
import cl.duoc.atlas.domain.model.Pagina;
import java.util.Optional;

/** Puerto de salida de clientes. Define QUE necesita el dominio, no COMO se implementa. */
public interface ClienteGateway {

  Optional<Cliente> findById(Long id);

  Optional<Cliente> findByRut(String rut);

  /**
   * Busca clientes activos cuyo nombre contenga el texto indicado.
   *
   * @param nombre texto a buscar (vacio = todos)
   * @param pagina numero de pagina desde 0
   * @param tamano elementos por pagina
   * @return pagina de clientes ordenada por nombre
   */
  Pagina<Cliente> buscarActivos(String nombre, int pagina, int tamano);

  Cliente save(Cliente cliente);
}
