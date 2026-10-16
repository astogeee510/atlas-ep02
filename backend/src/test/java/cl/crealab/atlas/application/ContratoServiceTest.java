package cl.crealab.atlas.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.crealab.atlas.application.dto.ContratoRequest;
import cl.crealab.atlas.application.dto.ContratoResponse;
import cl.crealab.atlas.domain.exception.NoEncontradoException;
import cl.crealab.atlas.domain.exception.ValidacionException;
import cl.crealab.atlas.domain.model.AccionAuditoria;
import cl.crealab.atlas.domain.model.Contrato;
import cl.crealab.atlas.domain.model.EstadoContrato;
import cl.crealab.atlas.domain.model.Rol;
import cl.crealab.atlas.infrastructure.persistence.ClienteRepository;
import cl.crealab.atlas.infrastructure.persistence.ContratoRepository;
import cl.crealab.atlas.infrastructure.persistence.DocumentoRepository;
import cl.crealab.atlas.soporte.Datos;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** CP-FUN-05: contratos, estado calculado y RB-3. */
@ExtendWith(MockitoExtension.class)
class ContratoServiceTest {

  @Mock private ContratoRepository contratos;
  @Mock private ClienteRepository clientes;
  @Mock private DocumentoRepository documentos;
  @Mock private ControlAcceso acceso;
  @Mock private AuditoriaService auditoria;

  private ContratoService servicio;

  @BeforeEach
  void setUp() {
    servicio =
        new ContratoService(contratos, clientes, documentos, acceso, auditoria, Datos.reloj(), 30);
  }

  private static ContratoRequest solicitud(Long clienteId, int diasTermino) {
    return new ContratoRequest(
        clienteId,
        " Suministro ",
        "desc",
        Datos.HOY.minusDays(10),
        Datos.HOY.plusDays(diasTermino));
  }

  @Test
  void listaContratosDelClienteConEstado() {
    when(clientes.findByIdAndEliminadoFalse(1L)).thenReturn(Optional.of(Datos.cliente(1L, 1L)));
    when(acceso.exigirMiembro(2L, 1L)).thenReturn(Rol.EDITOR);
    when(contratos.findByClienteIdAndEliminadoFalseOrderByFechaTerminoAsc(1L))
        .thenReturn(List.of(Datos.contrato(3L, 1L, 1L, Datos.HOY.plusDays(10))));
    when(documentos.findByContratoId(3L)).thenReturn(Optional.empty());
    List<ContratoResponse> lista = servicio.listarPorCliente(2L, 1L);
    assertThat(lista).hasSize(1);
    assertThat(lista.get(0).estado()).isEqualTo(EstadoContrato.POR_VENCER);
    assertThat(lista.get(0).tieneDocumento()).isFalse();
  }

  @Test
  void creaContratoActivo() {
    when(clientes.findByIdAndEliminadoFalse(1L)).thenReturn(Optional.of(Datos.cliente(1L, 1L)));
    when(contratos.save(any(Contrato.class)))
        .thenAnswer(
            inv -> {
              Contrato c = inv.getArgument(0);
              c.setId(7L);
              return c;
            });
    when(documentos.findByContratoId(7L)).thenReturn(Optional.empty());
    ContratoResponse creado = servicio.crear(1L, solicitud(1L, 200));
    assertThat(creado.id()).isEqualTo(7L);
    assertThat(creado.titulo()).isEqualTo("Suministro");
    assertThat(creado.estado()).isEqualTo(EstadoContrato.ACTIVO);
    verify(acceso).exigirAdmin(1L, 1L);
    verify(auditoria)
        .registrar(
            eq(1L), eq(1L), eq(AccionAuditoria.CREAR_CONTRATO), eq("contrato"), eq(7L), any());
  }

  @Test
  void rechazaFechasInvertidas() {
    when(clientes.findByIdAndEliminadoFalse(1L)).thenReturn(Optional.of(Datos.cliente(1L, 1L)));
    ContratoRequest invertida =
        new ContratoRequest(1L, "x", null, Datos.HOY, Datos.HOY.minusDays(1));
    assertThatThrownBy(() -> servicio.crear(1L, invertida)).isInstanceOf(ValidacionException.class);
    verify(contratos, never()).save(any());
  }

  @Test
  void aceptaInicioYTerminoElMismoDia() {
    when(clientes.findByIdAndEliminadoFalse(1L)).thenReturn(Optional.of(Datos.cliente(1L, 1L)));
    when(contratos.save(any(Contrato.class))).thenAnswer(inv -> inv.getArgument(0));
    when(documentos.findByContratoId(any())).thenReturn(Optional.empty());
    ContratoRequest mismoDia = new ContratoRequest(1L, "x", null, Datos.HOY, Datos.HOY);
    assertThat(servicio.crear(1L, mismoDia).estado()).isEqualTo(EstadoContrato.POR_VENCER);
  }

  @Test
  void clienteInexistente() {
    when(clientes.findByIdAndEliminadoFalse(5L)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> servicio.crear(1L, solicitud(5L, 10)))
        .isInstanceOf(NoEncontradoException.class);
  }

  @Test
  void contratoNoPuedeCambiarDeCliente() {
    when(contratos.findByIdAndEliminadoFalse(3L))
        .thenReturn(Optional.of(Datos.contrato(3L, 1L, 1L, Datos.HOY)));
    assertThatThrownBy(() -> servicio.actualizar(1L, 3L, solicitud(2L, 10)))
        .isInstanceOf(ValidacionException.class)
        .hasMessageContaining("RB-3");
  }

  @Test
  void actualizaContrato() {
    Contrato existente = Datos.contrato(3L, 1L, 1L, Datos.HOY);
    when(contratos.findByIdAndEliminadoFalse(3L)).thenReturn(Optional.of(existente));
    when(contratos.save(existente)).thenReturn(existente);
    when(documentos.findByContratoId(3L)).thenReturn(Optional.of(Datos.documento(1L, 3L)));
    ContratoResponse actualizado = servicio.actualizar(1L, 3L, solicitud(1L, 400));
    assertThat(actualizado.fechaTermino()).isEqualTo(Datos.HOY.plusDays(400));
    assertThat(actualizado.tieneDocumento()).isTrue();
  }

  @Test
  void eliminacionLogica() {
    Contrato existente = Datos.contrato(3L, 1L, 1L, Datos.HOY);
    when(contratos.findByIdAndEliminadoFalse(3L)).thenReturn(Optional.of(existente));
    servicio.eliminar(1L, 3L);
    assertThat(existente.isEliminado()).isTrue();
    verify(contratos).save(existente);
  }

  @Test
  void obtieneContrato() {
    when(contratos.findByIdAndEliminadoFalse(3L))
        .thenReturn(Optional.of(Datos.contrato(3L, 1L, 1L, Datos.HOY.minusDays(1))));
    when(acceso.exigirMiembro(1L, 1L)).thenReturn(Rol.ADMIN);
    when(documentos.findByContratoId(3L)).thenReturn(Optional.empty());
    assertThat(servicio.obtener(1L, 3L).estado()).isEqualTo(EstadoContrato.EXPIRADO);
  }
}
