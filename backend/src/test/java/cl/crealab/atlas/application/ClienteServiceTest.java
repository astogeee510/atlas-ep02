package cl.crealab.atlas.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.crealab.atlas.application.dto.ClienteRequest;
import cl.crealab.atlas.application.dto.ClienteResponse;
import cl.crealab.atlas.application.dto.PaginaResponse;
import cl.crealab.atlas.domain.exception.AccesoDenegadoException;
import cl.crealab.atlas.domain.exception.NoEncontradoException;
import cl.crealab.atlas.domain.exception.ValidacionException;
import cl.crealab.atlas.domain.model.AccionAuditoria;
import cl.crealab.atlas.domain.model.Cliente;
import cl.crealab.atlas.domain.model.Contrato;
import cl.crealab.atlas.domain.model.Rol;
import cl.crealab.atlas.infrastructure.persistence.ClienteRepository;
import cl.crealab.atlas.infrastructure.persistence.ContratoRepository;
import cl.crealab.atlas.soporte.Datos;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/** CP-FUN-03 / CP-FUN-04: gestion de clientes (RF-4). */
@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

  @Mock private ClienteRepository clientes;
  @Mock private ContratoRepository contratos;
  @Mock private ControlAcceso acceso;
  @Mock private AuditoriaService auditoria;

  private ClienteService servicio;

  @BeforeEach
  void setUp() {
    servicio = new ClienteService(clientes, contratos, acceso, auditoria, Datos.reloj());
  }

  private static ClienteRequest solicitud(String rut) {
    return new ClienteRequest(
        "Ferreteria Los Andes",
        rut,
        "Mayorista",
        "Av. Matta 500",
        "compras@ferreteria.cl",
        "+56 9 1234 5678",
        "Banco Estado",
        "123456789");
  }

  @Test
  void editorVeElNumeroDeCuentaEnmascarado() {
    when(acceso.exigirMiembro(2L, 1L)).thenReturn(Rol.EDITOR);
    when(clientes.findByEmpresaIdAndEliminadoFalseAndNombreContainingIgnoreCase(
            eq(1L), eq("fer"), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(Datos.cliente(1L, 1L)), PageRequest.of(0, 10), 1));
    PaginaResponse<ClienteResponse> pagina = servicio.listar(2L, 1L, " fer ", 0, 10);
    assertThat(pagina.totalElementos()).isEqualTo(1);
    assertThat(pagina.contenido().get(0).numeroCuenta()).isEqualTo("****6789");
  }

  @Test
  void listadoLimitaElTamanoDePagina() {
    when(acceso.exigirMiembro(1L, 1L)).thenReturn(Rol.ADMIN);
    ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
    when(clientes.findByEmpresaIdAndEliminadoFalseAndNombreContainingIgnoreCase(
            eq(1L), eq(""), captor.capture()))
        .thenReturn(new PageImpl<>(List.of()));
    servicio.listar(1L, 1L, null, -1, 500);
    assertThat(captor.getValue().getPageSize()).isEqualTo(50);
    assertThat(captor.getValue().getPageNumber()).isZero();
  }

  @Test
  void administradorVeLaCuentaCompleta() {
    when(clientes.findByIdAndEliminadoFalse(1L)).thenReturn(Optional.of(Datos.cliente(1L, 1L)));
    when(acceso.exigirMiembro(1L, 1L)).thenReturn(Rol.ADMIN);
    assertThat(servicio.obtener(1L, 1L).numeroCuenta()).isEqualTo("123456789");
  }

  @Test
  void creaClienteNormalizandoElRut() {
    when(clientes.save(any(Cliente.class)))
        .thenAnswer(
            inv -> {
              Cliente c = inv.getArgument(0);
              c.setId(9L);
              return c;
            });
    ClienteResponse creado = servicio.crear(1L, 1L, solicitud("76.123.456-0"));
    assertThat(creado.id()).isEqualTo(9L);
    assertThat(creado.rut()).isEqualTo("76123456-0");
    assertThat(creado.banco()).isEqualTo("Banco Estado");
    verify(acceso).exigirAdmin(1L, 1L);
    verify(auditoria)
        .registrar(eq(1L), eq(1L), eq(AccionAuditoria.CREAR_CLIENTE), eq("cliente"), eq(9L), any());
  }

  @Test
  void rechazaRutInvalido() {
    assertThatThrownBy(() -> servicio.crear(1L, 1L, solicitud("76.123.456-9")))
        .isInstanceOf(ValidacionException.class);
    verify(clientes, never()).save(any());
  }

  @Test
  void editorNoPuedeCrear() {
    doThrow(new AccesoDenegadoException("no")).when(acceso).exigirAdmin(2L, 1L);
    assertThatThrownBy(() -> servicio.crear(2L, 1L, solicitud(null)))
        .isInstanceOf(AccesoDenegadoException.class);
    verify(clientes, never()).save(any());
  }

  @Test
  void actualizaCliente() {
    Cliente existente = Datos.cliente(1L, 1L);
    when(clientes.findByIdAndEliminadoFalse(1L)).thenReturn(Optional.of(existente));
    when(clientes.save(existente)).thenReturn(existente);
    ClienteResponse actualizado = servicio.actualizar(1L, 1L, solicitud(""));
    assertThat(actualizado.descripcion()).isEqualTo("Mayorista");
    assertThat(actualizado.rut()).isNull();
    assertThat(existente.getActualizadoEn()).isEqualTo(Datos.AHORA);
    verify(auditoria)
        .registrar(
            eq(1L), eq(1L), eq(AccionAuditoria.EDITAR_CLIENTE), eq("cliente"), eq(1L), any());
  }

  @Test
  void clienteInexistenteOEliminado() {
    when(clientes.findByIdAndEliminadoFalse(99L)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> servicio.actualizar(1L, 99L, solicitud(null)))
        .isInstanceOf(NoEncontradoException.class);
  }

  @Test
  void eliminacionEsLogicaYAlcanzaASusContratos() {
    Cliente existente = Datos.cliente(1L, 1L);
    Contrato c1 = Datos.contrato(1L, 1L, 1L, Datos.HOY);
    Contrato c2 = Datos.contrato(2L, 1L, 1L, Datos.HOY);
    when(clientes.findByIdAndEliminadoFalse(1L)).thenReturn(Optional.of(existente));
    when(contratos.findByClienteIdAndEliminadoFalseOrderByFechaTerminoAsc(1L))
        .thenReturn(List.of(c1, c2));
    servicio.eliminar(1L, 1L);
    assertThat(existente.isEliminado()).isTrue();
    assertThat(c1.isEliminado()).isTrue();
    assertThat(c2.isEliminado()).isTrue();
    verify(clientes).save(existente);
    verify(contratos).saveAll(anyList());
    verify(clientes, never()).delete(any());
    verify(auditoria)
        .registrar(
            eq(1L),
            eq(1L),
            eq(AccionAuditoria.ELIMINAR_CLIENTE),
            eq("cliente"),
            eq(1L),
            contains("2 contratos"));
  }
}
