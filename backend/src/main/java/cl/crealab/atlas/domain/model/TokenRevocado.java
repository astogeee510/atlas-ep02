package cl.crealab.atlas.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** Identificador (jti) de un JWT invalidado por logout (RF-1.2). */
@Entity
@Table(name = "token_revocado")
public class TokenRevocado {

  @Id
  @Column(length = 64)
  private String jti;

  @Column(name = "expira_en", nullable = false)
  private Instant expiraEn;

  public TokenRevocado() {
    // requerido por JPA
  }

  public TokenRevocado(String jti, Instant expiraEn) {
    this.jti = jti;
    this.expiraEn = expiraEn;
  }

  public String getJti() {
    return jti;
  }

  public Instant getExpiraEn() {
    return expiraEn;
  }
}
