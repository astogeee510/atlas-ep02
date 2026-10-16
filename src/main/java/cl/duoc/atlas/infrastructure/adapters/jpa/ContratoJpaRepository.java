package cl.duoc.atlas.infrastructure.adapters.jpa;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repositorio Spring Data JPA de contratos. */
public interface ContratoJpaRepository extends JpaRepository<ContratoData, Long> {

  List<ContratoData> findByClienteIdOrderByFechaTerminoAsc(Long clienteId);
}
