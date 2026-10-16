package cl.crealab.atlas.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.crealab.atlas.application.dto.AuditoriaResponse;
import cl.crealab.atlas.application.dto.PaginaResponse;
import cl.crealab.atlas.domain.exception.AccesoDenegadoException;
import cl.crealab.atlas.domain.model.AccionAuditoria;
import cl.crealab.atlas.domain.model.RegistroAuditoria;
import cl.crealab.atlas.infrastructure.persistence.AuditoriaRepository;
import cl.crealab.atlas.soporte.Datos;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/** CP-INT-03: auditoria de acciones criticas (RF-6.1). */
@ExtendWith(MockitoExtension.class)
class AuditoriaServiceTest {

  @Mock private AuditoriaRepository repositorio;
  @Mock private ControlAcceso acceso;

  private AuditoriaService servicio;

  @BeforeEach
  void setUp() {
    servicio = new AuditoriaService(repositorio, acceso, Datos.reloj());
  }

  @Test
  void registraAccionConFechaDelReloj() {
    servicio.registrar(1L, 2L, AccionAuditoria.CREAR_CLIENTE, "cliente", 3L, "detalle");
    ArgumentCaptor<RegistroAuditoria> captor = ArgumentCaptor.forClass(RegistroAuditoria.class);
    verify(repositorio).save(captor.capture());
    RegistroAuditoria registro = captor.getValue();
    assertThat(registro.getFecha()).isEqualTo(Datos.AHORA);
    assertThat(registro.getEmpresaId()).isEqualTo(1L);
    assertThat(registro.getUsuarioId()).isEqualTo(2L);
    assertThat(registro.getEntidad()).isEqualTo("cliente");
    assertThat(registro.getEntidadId()).isEqualTo(3L);
    assertThat(registro.getDetalle()).isEqualTo("detalle");
  }

  @Test
  void administradorListaLaAuditoria() {
    RegistroAuditoria registro =
        new RegistroAuditoria(
            1L, 2L, AccionAuditoria.ELIMINAR_CLIENTE, "cliente", 3L, "x", Datos.AHORA);
    when(repositorio.findByEmpresaIdOrderByFechaDesc(eq(1L), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(registro), PageRequest.of(0, 20), 1));
    PaginaResponse<AuditoriaResponse> pagina = servicio.listar(1L, 1L, -3, 500);
    assertThat(pagina.contenido()).hasSize(1);
    assertThat(pagina.contenido().get(0).accion()).isEqualTo(AccionAuditoria.ELIMINAR_CLIENTE);
    verify(acceso).exigirAdmin(1L, 1L);
  }

  @Test
  void editorNoVeLaAuditoria() {
    doThrow(new AccesoDenegadoException("no")).when(acceso).exigirAdmin(2L, 1L);
    assertThatThrownBy(() -> servicio.listar(2L, 1L, 0, 20))
        .isInstanceOf(AccesoDenegadoException.class);
  }
}
