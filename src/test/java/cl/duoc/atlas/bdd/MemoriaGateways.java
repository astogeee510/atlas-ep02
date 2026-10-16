package cl.duoc.atlas.bdd;

import cl.duoc.atlas.domain.gateway.ClienteGateway;
import cl.duoc.atlas.domain.gateway.ContratoGateway;
import cl.duoc.atlas.domain.gateway.TokenGateway;
import cl.duoc.atlas.domain.gateway.UsuarioGateway;
import cl.duoc.atlas.domain.model.Cliente;
import cl.duoc.atlas.domain.model.Contrato;
import cl.duoc.atlas.domain.model.Pagina;
import cl.duoc.atlas.domain.model.Sesion;
import cl.duoc.atlas.domain.model.Usuario;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Implementaciones en memoria de los puertos para las pruebas BDD. Evitan levantar base de datos:
 * los escenarios son rapidos y deterministas.
 */
final class MemoriaGateways {

  private MemoriaGateways() {}

  /** Clientes en memoria. */
  static final class Clientes implements ClienteGateway {

    private final Map<Long, Cliente> datos = new LinkedHashMap<>();
    private long secuencia;

    @Override
    public Optional<Cliente> findById(Long id) {
      return Optional.ofNullable(datos.get(id));
    }

    @Override
    public Optional<Cliente> findByRut(String rut) {
      return datos.values().stream().filter(c -> c.getRut().equals(rut)).findFirst();
    }

    @Override
    public Pagina<Cliente> buscarActivos(String nombre, int pagina, int tamano) {
      List<Cliente> activos =
          datos.values().stream()
              .filter(Cliente::isActivo)
              .filter(
                  c ->
                      c.getNombre()
                          .toLowerCase(Locale.ROOT)
                          .contains(nombre.toLowerCase(Locale.ROOT)))
              .toList();
      return new Pagina<>(activos, pagina, tamano, activos.size());
    }

    @Override
    public Cliente save(Cliente cliente) {
      if (cliente.getId() == null) {
        cliente.setId(++secuencia);
      }
      datos.put(cliente.getId(), cliente);
      return cliente;
    }
  }

  /** Contratos en memoria. */
  static final class Contratos implements ContratoGateway {

    private final Map<Long, Contrato> datos = new LinkedHashMap<>();
    private long secuencia;

    @Override
    public Optional<Contrato> findById(Long id) {
      return Optional.ofNullable(datos.get(id));
    }

    @Override
    public List<Contrato> findByClienteId(Long clienteId) {
      return new ArrayList<>(
          datos.values().stream().filter(c -> c.getClienteId().equals(clienteId)).toList());
    }

    @Override
    public Contrato save(Contrato contrato) {
      if (contrato.getId() == null) {
        contrato.setId(++secuencia);
      }
      datos.put(contrato.getId(), contrato);
      return contrato;
    }

    @Override
    public void deleteById(Long id) {
      datos.remove(id);
    }
  }

  /** Usuarios en memoria. */
  static final class Usuarios implements UsuarioGateway {

    private final Map<String, Usuario> datos = new LinkedHashMap<>();

    @Override
    public Optional<Usuario> findByNombreUsuario(String nombreUsuario) {
      return Optional.ofNullable(datos.get(nombreUsuario));
    }

    @Override
    public Usuario save(Usuario usuario) {
      datos.put(usuario.getNombreUsuario(), usuario);
      return usuario;
    }
  }

  /** Emisor de tokens falso: devuelve un token legible con el usuario. */
  static final class Tokens implements TokenGateway {

    @Override
    public Sesion emitir(Usuario usuario) {
      return new Sesion(
          "token-" + usuario.getNombreUsuario(),
          Instant.parse("2026-10-16T12:15:00Z"),
          usuario.getNombreUsuario(),
          usuario.getRol());
    }

    @Override
    public void revocar(String token) {
      // sin efecto en memoria
    }
  }
}
