package cl.crealab.atlas.infrastructure.persistence;

import cl.crealab.atlas.domain.model.Membresia;
import cl.crealab.atlas.domain.model.Rol;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acceso a la relacion empresa_usuario. */
public interface MembresiaRepository extends JpaRepository<Membresia, Long> {

  Optional<Membresia> findByEmpresaIdAndUsuarioId(Long empresaId, Long usuarioId);

  List<Membresia> findByUsuarioId(Long usuarioId);

  List<Membresia> findByEmpresaId(Long empresaId);

  long countByEmpresaIdAndRol(Long empresaId, Rol rol);
}
