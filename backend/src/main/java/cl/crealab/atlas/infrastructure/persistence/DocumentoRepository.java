package cl.crealab.atlas.infrastructure.persistence;

import cl.crealab.atlas.domain.model.Documento;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acceso a la referencia en BD de los documentos (RF-5.3). */
public interface DocumentoRepository extends JpaRepository<Documento, Long> {

  Optional<Documento> findByContratoId(Long contratoId);
}
