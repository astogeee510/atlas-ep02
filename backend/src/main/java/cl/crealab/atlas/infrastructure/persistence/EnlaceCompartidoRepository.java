package cl.crealab.atlas.infrastructure.persistence;

import cl.crealab.atlas.domain.model.EnlaceCompartido;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acceso a los enlaces compartidos (RF-5.5). */
public interface EnlaceCompartidoRepository extends JpaRepository<EnlaceCompartido, Long> {

  Optional<EnlaceCompartido> findByToken(String token);

  List<EnlaceCompartido> findByDocumentoIdAndRevocadoFalse(Long documentoId);
}
