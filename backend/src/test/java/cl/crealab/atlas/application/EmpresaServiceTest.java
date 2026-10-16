package cl.crealab.atlas.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.crealab.atlas.application.dto.DashboardResponse;
import cl.crealab.atlas.application.dto.EmpresaRequest;
import cl.crealab.atlas.application.dto.EmpresaResponse;
import cl.crealab.atlas.domain.exception.ConflictoException;
import cl.crealab.atlas.domain.exception.NoEncontradoException;
import cl.crealab.atlas.domain.model.AccionAuditoria;
import cl.crealab.atlas.domain.model.Empresa;
import cl.crealab.atlas.domain.model.Membresia;
import cl.crealab.atlas.domain.model.Rol;
import cl.crealab.atlas.infrastructure.persistence.ClienteRepository;
import cl.crealab.atlas.infrastructure.persistence.ContratoRepository;
import cl.crealab.atlas.infrastructure.persistence.EmpresaRepository;
import cl.crealab.atlas.infrastructure.persistence.MembresiaRepository;
import cl.crealab.atlas.soporte.Datos;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** CP-FUN-01: creacion de empresa y primer administrador (RF-2.2, RF-2.3, RB-1). */
@ExtendWith(MockitoExtension.class)
class EmpresaServiceTest {

  @Mock private EmpresaRepository empresas;
  @Mock private MembresiaRepository membresias;
  @Mock private ClienteRepository clientes;
  @Mock private ContratoRepository contratos;
  @Mock private ControlAcceso acceso;
  @Mock private AuditoriaService auditoria;

  private EmpresaService servicio;

  @BeforeEach
  void setUp() {
    servicio =
        new EmpresaService(
            empresas, membresias, clientes, contratos, acceso, auditoria, Datos.reloj(), 30);
  }

  @Test
  void quienCreaLaEmpresaQuedaComoAdministrador() {
    when(empresas.existsByCorreoIgnoreCase("contacto@pyme.cl")).thenReturn(false);
    when(empresas.save(any(Empresa.class)))
        .thenAnswer(
            inv -> {
              Empresa e = inv.getArgument(0);
              e.setId(5L);
              return e;
            });
    EmpresaResponse respuesta =
        servicio.crear(7L, new EmpresaRequest(" Pyme SpA ", "Dir", null, "Contacto@pyme.cl"));
    assertThat(respuesta.id()).isEqualTo(5L);
    assertThat(respuesta.nombre()).isEqualTo("Pyme SpA");
    assertThat(respuesta.correo()).isEqualTo("contacto@pyme.cl");
    verify(membresias)
        .save(
            argThat(
                (Membresia m) ->
                    m.getRol() == Rol.ADMIN && m.getUsuarioId() == 7L && m.getEmpresaId() == 5L));
    verify(auditoria)
        .registrar(eq(5L), eq(7L), eq(AccionAuditoria.CREAR_EMPRESA), eq("empresa"), eq(5L), any());
  }

  @Test
  void correoPrincipalNoPuedeRepetirse() {
    when(empresas.existsByCorreoIgnoreCase("c@x.cl")).thenReturn(true);
    assertThatThrownBy(() -> servicio.crear(1L, new EmpresaRequest("Pyme", null, null, "c@x.cl")))
        .isInstanceOf(ConflictoException.class);
    verify(empresas, never()).save(any());
  }

  @Test
  void obtieneEmpresaVisible() {
    when(acceso.exigirMiembro(1L, 1L)).thenReturn(Rol.EDITOR);
    when(empresas.findById(1L)).thenReturn(Optional.of(Datos.empresa(1L)));
    assertThat(servicio.obtener(1L, 1L).nombre()).isEqualTo("CreaLab Demo SpA");
  }

  @Test
  void empresaInexistente() {
    when(acceso.exigirMiembro(1L, 9L)).thenReturn(Rol.ADMIN);
    when(empresas.findById(9L)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> servicio.obtener(1L, 9L)).isInstanceOf(NoEncontradoException.class);
  }

  @Test
  void dashboardCuentaContratosPorEstado() {
    when(acceso.exigirMiembro(1L, 1L)).thenReturn(Rol.ADMIN);
    when(clientes.countByEmpresaIdAndEliminadoFalse(1L)).thenReturn(3L);
    when(contratos.findByEmpresaIdAndEliminadoFalse(1L))
        .thenReturn(
            List.of(
                Datos.contrato(1L, 1L, 1L, Datos.HOY.plusDays(100)),
                Datos.contrato(2L, 1L, 1L, Datos.HOY.plusDays(31)),
                Datos.contrato(3L, 1L, 1L, Datos.HOY.plusDays(10)),
                Datos.contrato(4L, 1L, 1L, Datos.HOY.minusDays(5))));
    DashboardResponse dashboard = servicio.dashboard(1L, 1L);
    assertThat(dashboard.totalClientes()).isEqualTo(3);
    assertThat(dashboard.contratosActivos()).isEqualTo(2);
    assertThat(dashboard.contratosPorVencer()).isEqualTo(1);
    assertThat(dashboard.contratosExpirados()).isEqualTo(1);
    assertThat(dashboard.rol()).isEqualTo(Rol.ADMIN);
  }
}
