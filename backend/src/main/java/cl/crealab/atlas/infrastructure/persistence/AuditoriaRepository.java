package cl.crealab.atlas.infrastructure.persistence;

import cl.crealab.atlas.domain.model.RegistroAuditoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acceso a los registros de auditoria (RF-6.1). */
public interface AuditoriaRepository extends JpaRepository<RegistroAuditoria, Long> {

  Page<RegistroAuditoria> findByEmpresaIdOrderByFechaDesc(Long empresaId, Pageable pageable);
}
