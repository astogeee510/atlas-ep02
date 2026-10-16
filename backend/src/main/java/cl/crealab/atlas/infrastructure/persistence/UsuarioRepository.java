package cl.crealab.atlas.infrastructure.persistence;

import cl.crealab.atlas.domain.model.Usuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acceso a datos de usuarios. */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

  Optional<Usuario> findByNombreUsuario(String nombreUsuario);

  boolean existsByNombreUsuario(String nombreUsuario);

  boolean existsByCorreoIgnoreCase(String correo);
}
