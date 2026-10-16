package cl.crealab.atlas.infrastructure.persistence;

import cl.crealab.atlas.domain.model.TokenRevocado;
import org.springframework.data.jpa.repository.JpaRepository;

/** Lista de tokens revocados por logout (RF-1.2). */
public interface TokenRevocadoRepository extends JpaRepository<TokenRevocado, String> {}
