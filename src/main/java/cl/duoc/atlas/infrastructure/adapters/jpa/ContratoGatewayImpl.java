package cl.duoc.atlas.infrastructure.adapters.jpa;

import cl.duoc.atlas.domain.gateway.ContratoGateway;
import cl.duoc.atlas.domain.model.Contrato;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/** Adaptador que implementa el puerto ContratoGateway usando JPA/SQLite. */
@Repository
public class ContratoGatewayImpl implements ContratoGateway {

  private final ContratoJpaRepository repository;
  private final ContratoMapper mapper;

  public ContratoGatewayImpl(ContratoJpaRepository repository, ContratoMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Override
  public Optional<Contrato> findById(Long id) {
    return repository.findById(id).map(mapper::toDomain);
  }

  @Override
  public List<Contrato> findByClienteId(Long clienteId) {
    return repository.findByClienteIdOrderByFechaTerminoAsc(clienteId).stream()
        .map(mapper::toDomain)
        .toList();
  }

  @Override
  public Contrato save(Contrato contrato) {
    return mapper.toDomain(repository.save(mapper.toData(contrato)));
  }

  @Override
  public void deleteById(Long id) {
    repository.deleteById(id);
  }
}
