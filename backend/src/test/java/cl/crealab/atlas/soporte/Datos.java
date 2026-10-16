package cl.crealab.atlas.soporte;

import cl.crealab.atlas.domain.model.Cliente;
import cl.crealab.atlas.domain.model.Contrato;
import cl.crealab.atlas.domain.model.Documento;
import cl.crealab.atlas.domain.model.Empresa;
import cl.crealab.atlas.domain.model.Usuario;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/** Datos sinteticos reutilizables en las pruebas. */
public final class Datos {

  /** Fecha fija de las pruebas: 16-10-2026 12:00 en Santiago. */
  public static final Instant AHORA = Instant.parse("2026-10-16T15:00:00Z");

  public static final ZoneId ZONA = ZoneId.of("America/Santiago");

  public static final LocalDate HOY = LocalDate.of(2026, 10, 16);

  private Datos() {}

  public static Clock reloj() {
    return Clock.fixed(AHORA, ZONA);
  }

  public static Usuario usuario(Long id, String nombre) {
    Usuario usuario = new Usuario(nombre, nombre + "@atlas.test", "$2a$12$hash", true);
    usuario.setId(id);
    return usuario;
  }

  public static Empresa empresa(Long id) {
    Empresa empresa = new Empresa("CreaLab Demo SpA", "Santiago", "+56 2 2345 6789", "c@x.cl");
    empresa.setId(id);
    return empresa;
  }

  public static Cliente cliente(Long id, Long empresaId) {
    Cliente cliente = new Cliente(empresaId, "Ferreteria Los Andes");
    cliente.setId(id);
    cliente.setNumeroCuenta("123456789");
    cliente.setBanco("Banco Estado");
    return cliente;
  }

  public static Contrato contrato(Long id, Long empresaId, Long clienteId, LocalDate termino) {
    Contrato contrato = new Contrato(empresaId, clienteId, "Suministro");
    contrato.setId(id);
    contrato.setFechaInicio(termino.minusYears(1));
    contrato.setFechaTermino(termino);
    return contrato;
  }

  public static Documento documento(Long id, Long contratoId) {
    Documento documento = new Documento(contratoId, "contrato.pdf", "application/pdf", 1L);
    documento.setId(id);
    documento.setRutaAlmacenamiento("ruta-" + id);
    documento.setTamanoBytes(10);
    documento.setSha256("0".repeat(64));
    return documento;
  }

  /** Bytes minimos de un PDF valido. */
  public static byte[] pdf() {
    return "%PDF-1.4\n%prueba\n".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
  }

  /** Bytes de un ejecutable de Windows (firma MZ). */
  public static byte[] ejecutable() {
    return new byte[] {0x4D, 0x5A, (byte) 0x90, 0x00, 0x03, 0x00};
  }
}
