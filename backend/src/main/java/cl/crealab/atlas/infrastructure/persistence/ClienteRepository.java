package cl.crealab.atlas.infrastructure.persistence;

import cl.crealab.atlas.domain.model.Cliente;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acceso a datos de clientes. Todas las consultas son parametrizadas (NFR-SEG-5). */
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

  Page<Cliente> findByEmpresaIdAndEliminadoFalseAndNombreContainingIgnoreCase(
      Long empresaId, String nombre, Pageable pageable);

  Optional<Cliente> findByIdAndEliminadoFalse(Long id);

  long countByEmpresaIdAndEliminadoFalse(Long empresaId);
}
