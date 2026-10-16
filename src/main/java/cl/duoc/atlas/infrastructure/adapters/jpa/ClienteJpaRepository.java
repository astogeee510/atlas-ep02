package cl.duoc.atlas.infrastructure.adapters.jpa;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio Spring Data JPA de clientes. Las consultas derivadas usan parametros enlazados, lo
 * que evita la inyeccion SQL (NFR-SEG-5).
 */
public interface ClienteJpaRepository extends JpaRepository<ClienteData, Long> {

  Optional<ClienteData> findByRut(String rut);

  Page<ClienteData> findByActivoTrueAndNombreContainingIgnoreCase(String nombre, Pageable pageable);
}
