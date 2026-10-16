package cl.crealab.atlas.infrastructure.persistence;

import cl.crealab.atlas.domain.model.Contrato;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acceso a datos de contratos. */
public interface ContratoRepository extends JpaRepository<Contrato, Long> {

  List<Contrato> findByClienteIdAndEliminadoFalseOrderByFechaTerminoAsc(Long clienteId);

  List<Contrato> findByEmpresaIdAndEliminadoFalse(Long empresaId);

  Optional<Contrato> findByIdAndEliminadoFalse(Long id);
}
