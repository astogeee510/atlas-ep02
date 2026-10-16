package cl.duoc.atlas.infrastructure.adapters.jpa;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repositorio Spring Data JPA de usuarios. */
public interface UsuarioJpaRepository extends JpaRepository<UsuarioData, Long> {

  Optional<UsuarioData> findByNombreUsuario(String nombreUsuario);
}
