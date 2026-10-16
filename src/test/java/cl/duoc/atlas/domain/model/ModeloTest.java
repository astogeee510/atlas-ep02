package cl.duoc.atlas.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ModeloTest {

  @Test
  void clienteExponeSusDatos() {
    Cliente c = new Cliente();
    c.setId(1L);
    c.setRut("12345678-5");
    c.setNombre("Ana");
    c.setDescripcion("d");
    c.setDireccion("dir");
    c.setEmail("a@b.cl");
    c.setTelefono("+56911111111");
    c.setActivo(false);

    assertThat(c.getId()).isEqualTo(1L);
    assertThat(c.getRut()).isEqualTo("12345678-5");
    assertThat(c.getNombre()).isEqualTo("Ana");
    assertThat(c.getDescripcion()).isEqualTo("d");
    assertThat(c.getDireccion()).isEqualTo("dir");
    assertThat(c.getEmail()).isEqualTo("a@b.cl");
    assertThat(c.getTelefono()).isEqualTo("+56911111111");
    assertThat(c.isActivo()).isFalse();
    assertThat(new Cliente().isActivo()).isTrue();
  }

  @Test
  void clienteIgualdadPorIdYRut() {
    Cliente a = new Cliente(1L, "1-9", "A", null, "x", "a@b.cl", "12345678", true);
    Cliente b = new Cliente(1L, "1-9", "B", null, "y", "c@d.cl", "87654321", false);
    Cliente c = new Cliente(2L, "1-9", "A", null, "x", "a@b.cl", "12345678", true);

    assertThat(a).isEqualTo(a).isEqualTo(b).hasSameHashCodeAs(b).isNotEqualTo(c);
    assertThat(a).isNotEqualTo("otro").isNotEqualTo(null);
  }

  @Test
  void contratoExponeSusDatosYCalculaEstado() {
    Contrato c = new Contrato();
    c.setId(5L);
    c.setClienteId(1L);
    c.setTitulo("T");
    c.setDescripcion("D");
    c.setFechaInicio(LocalDate.of(2026, 1, 1));
    c.setFechaTermino(LocalDate.of(2026, 12, 31));

    assertThat(c.getId()).isEqualTo(5L);
    assertThat(c.getClienteId()).isEqualTo(1L);
    assertThat(c.getTitulo()).isEqualTo("T");
    assertThat(c.getDescripcion()).isEqualTo("D");
    assertThat(c.getFechaInicio()).isEqualTo(LocalDate.of(2026, 1, 1));
    assertThat(c.getFechaTermino()).isEqualTo(LocalDate.of(2026, 12, 31));
    assertThat(c.tieneFechasValidas()).isTrue();
    assertThat(c.estado(LocalDate.of(2026, 6, 1))).isEqualTo(EstadoContrato.ACTIVO);
    assertThat(c.estado(LocalDate.of(2027, 1, 1))).isEqualTo(EstadoContrato.EXPIRADO);
  }

  @Test
  void contratoConFechasIgualesEsValidoYConTerminoAnteriorNo() {
    LocalDate dia = LocalDate.of(2026, 5, 5);
    assertThat(new Contrato(null, 1L, "T", null, dia, dia).tieneFechasValidas()).isTrue();
    assertThat(new Contrato(null, 1L, "T", null, dia, dia.minusDays(1)).tieneFechasValidas())
        .isFalse();
  }

  @Test
  void contratoIgualdadPorId() {
    LocalDate d = LocalDate.of(2026, 1, 1);
    Contrato a = new Contrato(1L, 1L, "A", null, d, d);
    Contrato b = new Contrato(1L, 2L, "B", null, d, d);
    Contrato c = new Contrato(2L, 1L, "A", null, d, d);

    assertThat(a).isEqualTo(a).isEqualTo(b).hasSameHashCodeAs(b).isNotEqualTo(c);
    assertThat(a).isNotEqualTo("otro");
  }

  @Test
  void usuarioExponeSusDatos() {
    Usuario u = new Usuario();
    u.setId(3L);
    u.setNombreUsuario("admin");
    u.setHashContrasena("$2a$12$hash");
    u.setRol(Rol.ADMIN);
    u.setActivo(false);

    assertThat(u.getId()).isEqualTo(3L);
    assertThat(u.getNombreUsuario()).isEqualTo("admin");
    assertThat(u.getHashContrasena()).isEqualTo("$2a$12$hash");
    assertThat(u.getRol()).isEqualTo(Rol.ADMIN);
    assertThat(u.isActivo()).isFalse();
    assertThat(new Usuario().isActivo()).isTrue();
  }

  @Test
  void paginaCalculaTotalDePaginasYCopiaElContenido() {
    List<String> datos = new ArrayList<>(List.of("a", "b"));
    Pagina<String> pagina = new Pagina<>(datos, 0, 10, 21);
    datos.add("c");

    assertThat(pagina.contenido()).containsExactly("a", "b");
    assertThat(pagina.totalPaginas()).isEqualTo(3);
    assertThat(new Pagina<>(List.of(), 0, 10, 0).totalPaginas()).isZero();
    assertThat(new Pagina<>(List.of(), 0, 10, 10).totalPaginas()).isEqualTo(1);
  }

  @Test
  void sesionGuardaSusDatos() {
    Instant expira = Instant.parse("2026-10-16T10:00:00Z");
    Sesion s = new Sesion("tok", expira, "admin", Rol.ADMIN);

    assertThat(s.token()).isEqualTo("tok");
    assertThat(s.expiraEn()).isEqualTo(expira);
    assertThat(s.nombreUsuario()).isEqualTo("admin");
    assertThat(s.rol()).isEqualTo(Rol.ADMIN);
  }
}
