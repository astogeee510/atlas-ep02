package cl.duoc.atlas.infrastructure.adapters.jpa;

import cl.duoc.atlas.domain.gateway.UsuarioGateway;
import cl.duoc.atlas.domain.model.Usuario;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/** Adaptador que implementa el puerto UsuarioGateway usando JPA/SQLite. */
@Repository
public class UsuarioGatewayImpl implements UsuarioGateway {

  private final UsuarioJpaRepository repository;
  private final UsuarioMapper mapper;

  public UsuarioGatewayImpl(UsuarioJpaRepository repository, UsuarioMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Override
  public Optional<Usuario> findByNombreUsuario(String nombreUsuario) {
    return repository.findByNombreUsuario(nombreUsuario).map(mapper::toDomain);
  }

  @Override
  public Usuario save(Usuario usuario) {
    return mapper.toDomain(repository.save(mapper.toData(usuario)));
  }
}
