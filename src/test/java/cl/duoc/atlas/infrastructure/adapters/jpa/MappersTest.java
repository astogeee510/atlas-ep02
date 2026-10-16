package cl.duoc.atlas.infrastructure.adapters.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import cl.duoc.atlas.domain.model.Cliente;
import cl.duoc.atlas.domain.model.Contrato;
import cl.duoc.atlas.domain.model.Rol;
import cl.duoc.atlas.domain.model.Usuario;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class MappersTest {

  @Test
  void clienteIdaYVuelta() {
    ClienteMapper mapper = new ClienteMapper();
    Cliente c = new Cliente(1L, "12345678-5", "N", "D", "Dir", "a@b.cl", "12345678", false);

    Cliente r = mapper.toDomain(mapper.toData(c));

    assertThat(r).usingRecursiveComparison().isEqualTo(c);
    assertThat(mapper.toDomain(null)).isNull();
    assertThat(mapper.toData(null)).isNull();
  }

  @Test
  void contratoIdaYVuelta() {
    ContratoMapper mapper = new ContratoMapper();
    Contrato c =
        new Contrato(2L, 1L, "T", "D", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));

    Contrato r = mapper.toDomain(mapper.toData(c));

    assertThat(r).usingRecursiveComparison().isEqualTo(c);
    assertThat(mapper.toDomain(null)).isNull();
    assertThat(mapper.toData(null)).isNull();
  }

  @Test
  void usuarioIdaYVuelta() {
    UsuarioMapper mapper = new UsuarioMapper();
    Usuario u = new Usuario(3L, "editor", "$2a$12$x", Rol.EDITOR, true);

    Usuario r = mapper.toDomain(mapper.toData(u));

    assertThat(r).usingRecursiveComparison().isEqualTo(u);
    assertThat(mapper.toDomain(null)).isNull();
    assertThat(mapper.toData(null)).isNull();
  }
}
