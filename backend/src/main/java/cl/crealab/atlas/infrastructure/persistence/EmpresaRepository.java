package cl.crealab.atlas.infrastructure.persistence;

import cl.crealab.atlas.domain.model.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acceso a datos de empresas. */
public interface EmpresaRepository extends JpaRepository<Empresa, Long> {

  boolean existsByCorreoIgnoreCase(String correo);
}
