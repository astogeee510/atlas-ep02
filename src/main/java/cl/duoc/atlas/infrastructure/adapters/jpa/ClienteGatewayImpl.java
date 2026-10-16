package cl.duoc.atlas.infrastructure.adapters.jpa;

import cl.duoc.atlas.domain.gateway.ClienteGateway;
import cl.duoc.atlas.domain.model.Cliente;
import cl.duoc.atlas.domain.model.Pagina;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

/** Adaptador que implementa el puerto ClienteGateway usando JPA/SQLite. */
@Repository
public class ClienteGatewayImpl implements ClienteGateway {

  private final ClienteJpaRepository repository;
  private final ClienteMapper mapper;

  public ClienteGatewayImpl(ClienteJpaRepository repository, ClienteMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Override
  public Optional<Cliente> findById(Long id) {
    return repository.findById(id).map(mapper::toDomain);
  }

  @Override
  public Optional<Cliente> findByRut(String rut) {
    return repository.findByRut(rut).map(mapper::toDomain);
  }

  @Override
  public Pagina<Cliente> buscarActivos(String nombre, int pagina, int tamano) {
    Page<ClienteData> resultado =
        repository.findByActivoTrueAndNombreContainingIgnoreCase(
            nombre, PageRequest.of(pagina, tamano, Sort.by("nombre")));
    return new Pagina<>(
        resultado.getContent().stream().map(mapper::toDomain).toList(),
        pagina,
        tamano,
        resultado.getTotalElements());
  }

  @Override
  public Cliente save(Cliente cliente) {
    return mapper.toDomain(repository.save(mapper.toData(cliente)));
  }
}
