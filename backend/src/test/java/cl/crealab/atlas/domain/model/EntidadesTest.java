package cl.crealab.atlas.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Test;

/** Reglas simples de las entidades. */
class EntidadesTest {

  private static final Instant AHORA = Instant.parse("2026-10-16T15:00:00Z");

  @Test
  void eliminacionLogicaDeClienteConservaElRegistro() {
    Cliente cliente = new Cliente(1L, "Ferreteria");
    cliente.eliminarLogicamente(AHORA);
    assertThat(cliente.isEliminado()).isTrue();
    assertThat(cliente.getEliminadoEn()).isEqualTo(AHORA);
    assertThat(cliente.getActualizadoEn()).isEqualTo(AHORA);
    assertThat(cliente.getNombre()).isEqualTo("Ferreteria");
  }

  @Test
  void enlaceVigenteAntesDeExpirar() {
    EnlaceCompartido enlace = new EnlaceCompartido(1L, "t", AHORA.plusSeconds(60), 1L);
    assertThat(enlace.esVigente(AHORA)).isTrue();
    assertThat(enlace.esVigente(AHORA.plusSeconds(60))).isFalse();
  }

  @Test
  void enlaceRevocadoNoEsVigente() {
    EnlaceCompartido enlace = new EnlaceCompartido(1L, "t", AHORA.plusSeconds(60), 1L);
    enlace.setRevocado(true);
    assertThat(enlace.esVigente(AHORA)).isFalse();
    assertThat(enlace.isRevocado()).isTrue();
  }

  @Test
  void entidadesExponenSusDatos() {
    Usuario usuario = new Usuario("ana", "ana@x.cl", "hash", true);
    usuario.setActivo(false);
    assertThat(usuario.isActivo()).isFalse();
    assertThat(usuario.isConsentimientoDatos()).isTrue();
    assertThat(usuario.getCreadoEn()).isNotNull();
    Membresia membresia = new Membresia(1L, 2L, Rol.EDITOR);
    membresia.setRol(Rol.ADMIN);
    assertThat(membresia.getRol()).isEqualTo(Rol.ADMIN);
    assertThat(membresia.getEmpresaId()).isEqualTo(1L);
    TokenRevocado token = new TokenRevocado("jti", AHORA);
    assertThat(token.getJti()).isEqualTo("jti");
    assertThat(token.getExpiraEn()).isEqualTo(AHORA);
    RegistroAuditoria registro =
        new RegistroAuditoria(1L, 2L, AccionAuditoria.CREAR_CLIENTE, "cliente", 3L, "d", AHORA);
    assertThat(registro.getAccion()).isEqualTo(AccionAuditoria.CREAR_CLIENTE);
    assertThat(registro.getFecha()).isEqualTo(AHORA);
  }
}
