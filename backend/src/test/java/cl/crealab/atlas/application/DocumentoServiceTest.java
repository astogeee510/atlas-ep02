package cl.crealab.atlas.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.crealab.atlas.application.dto.ArchivoDescarga;
import cl.crealab.atlas.application.dto.DocumentoResponse;
import cl.crealab.atlas.application.dto.EnlaceResponse;
import cl.crealab.atlas.domain.exception.AccesoDenegadoException;
import cl.crealab.atlas.domain.exception.ArchivoNoPermitidoException;
import cl.crealab.atlas.domain.exception.EnlaceExpiradoException;
import cl.crealab.atlas.domain.exception.NoEncontradoException;
import cl.crealab.atlas.domain.exception.ValidacionException;
import cl.crealab.atlas.domain.model.AccionAuditoria;
import cl.crealab.atlas.domain.model.Documento;
import cl.crealab.atlas.domain.model.EnlaceCompartido;
import cl.crealab.atlas.domain.model.Rol;
import cl.crealab.atlas.domain.service.TipoDocumento;
import cl.crealab.atlas.infrastructure.persistence.ContratoRepository;
import cl.crealab.atlas.infrastructure.persistence.DocumentoRepository;
import cl.crealab.atlas.infrastructure.persistence.EnlaceCompartidoRepository;
import cl.crealab.atlas.infrastructure.storage.AlmacenamientoArchivos;
import cl.crealab.atlas.soporte.Datos;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** CP-INT-01, CP-INT-02, CP-SEG-05: documentos y enlaces compartidos (RF-5.2, 5.3, 5.5). */
@ExtendWith(MockitoExtension.class)
class DocumentoServiceTest {

  @Mock private ContratoRepository contratos;
  @Mock private DocumentoRepository documentos;
  @Mock private EnlaceCompartidoRepository enlaces;
  @Mock private AlmacenamientoArchivos almacenamiento;
  @Mock private ControlAcceso acceso;
  @Mock private AuditoriaService auditoria;

  private DocumentoService servicio;

  @BeforeEach
  void setUp() {
    servicio = crear(10_485_760L);
  }

  private DocumentoService crear(long maxBytes) {
    return new DocumentoService(
        contratos, documentos, enlaces, almacenamiento, acceso, auditoria, Datos.reloj(), maxBytes);
  }

  private void conContrato() {
    when(contratos.findByIdAndEliminadoFalse(3L))
        .thenReturn(Optional.of(Datos.contrato(3L, 1L, 1L, Datos.HOY)));
  }

  @Test
  void subePdfYGuardaReferencia() {
    conContrato();
    when(documentos.findByContratoId(3L)).thenReturn(Optional.empty());
    when(almacenamiento.guardar(any(byte[].class))).thenReturn("uuid-1");
    when(documentos.save(any(Documento.class)))
        .thenAnswer(
            inv -> {
              Documento d = inv.getArgument(0);
              d.setId(20L);
              return d;
            });
    DocumentoResponse respuesta = servicio.subir(1L, 3L, "contrato firmado.pdf", Datos.pdf());
    assertThat(respuesta.id()).isEqualTo(20L);
    assertThat(respuesta.tipoContenido()).isEqualTo("application/pdf");
    assertThat(respuesta.sha256()).hasSize(64);
    assertThat(respuesta.tamanoBytes()).isEqualTo(Datos.pdf().length);
    verify(acceso).exigirAdmin(1L, 1L);
    verify(auditoria)
        .registrar(
            eq(1L),
            eq(1L),
            eq(AccionAuditoria.SUBIR_DOCUMENTO),
            eq("documento"),
            eq(20L),
            eq("contrato firmado.pdf"));
  }

  @Test
  void reemplazaElDocumentoAnterior() {
    conContrato();
    Documento anterior = Datos.documento(5L, 3L);
    when(documentos.findByContratoId(3L)).thenReturn(Optional.of(anterior));
    when(almacenamiento.guardar(any(byte[].class))).thenReturn("uuid-2");
    when(documentos.save(any(Documento.class))).thenAnswer(inv -> inv.getArgument(0));
    servicio.subir(1L, 3L, "nuevo.pdf", Datos.pdf());
    verify(almacenamiento).eliminar("ruta-5");
    verify(documentos).delete(anterior);
    verify(documentos).flush();
  }

  @Test
  void rechazaEjecutableRenombradoComoPdf() {
    conContrato();
    assertThatThrownBy(() -> servicio.subir(1L, 3L, "factura.pdf", Datos.ejecutable()))
        .isInstanceOf(ArchivoNoPermitidoException.class);
    verify(almacenamiento, never()).guardar(any());
  }

  @Test
  void rechazaArchivoVacio() {
    conContrato();
    assertThatThrownBy(() -> servicio.subir(1L, 3L, "a.pdf", new byte[0]))
        .isInstanceOf(ValidacionException.class);
  }

  @Test
  void rechazaArchivoQueSuperaElMaximo() {
    servicio = crear(5L);
    conContrato();
    assertThatThrownBy(() -> servicio.subir(1L, 3L, "a.pdf", Datos.pdf()))
        .isInstanceOf(ValidacionException.class)
        .hasMessageContaining("10 MB");
  }

  @Test
  void editorNoPuedeSubir() {
    conContrato();
    doThrow(new AccesoDenegadoException("no")).when(acceso).exigirAdmin(2L, 1L);
    assertThatThrownBy(() -> servicio.subir(2L, 3L, "a.pdf", Datos.pdf()))
        .isInstanceOf(AccesoDenegadoException.class);
  }

  @Test
  void descargaQuedaAuditada() {
    conContrato();
    when(acceso.exigirMiembro(2L, 1L)).thenReturn(Rol.EDITOR);
    when(documentos.findByContratoId(3L)).thenReturn(Optional.of(Datos.documento(5L, 3L)));
    when(almacenamiento.leer("ruta-5")).thenReturn(Datos.pdf());
    ArchivoDescarga archivo = servicio.descargar(2L, 3L);
    assertThat(archivo.nombre()).isEqualTo("contrato.pdf");
    assertThat(archivo.contenido()).isEqualTo(Datos.pdf());
    verify(auditoria)
        .registrar(
            eq(1L),
            eq(2L),
            eq(AccionAuditoria.DESCARGAR_DOCUMENTO),
            eq("documento"),
            eq(5L),
            any());
  }

  @Test
  void metadatosDeContratoSinDocumento() {
    conContrato();
    when(acceso.exigirMiembro(1L, 1L)).thenReturn(Rol.ADMIN);
    when(documentos.findByContratoId(3L)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> servicio.obtener(1L, 3L)).isInstanceOf(NoEncontradoException.class);
  }

  @Test
  void creaEnlaceDe24HorasPorDefecto() {
    conContrato();
    when(documentos.findByContratoId(3L)).thenReturn(Optional.of(Datos.documento(5L, 3L)));
    EnlaceResponse enlace = servicio.crearEnlace(1L, 3L, null);
    assertThat(enlace.expiraEn()).isEqualTo(Datos.AHORA.plus(Duration.ofHours(24)));
    assertThat(enlace.token()).hasSize(43);
    assertThat(enlace.url()).isEqualTo("/api/compartido/" + enlace.token());
    verify(enlaces).save(any(EnlaceCompartido.class));
  }

  @Test
  void enlacesSonUnicos() {
    conContrato();
    when(documentos.findByContratoId(3L)).thenReturn(Optional.of(Datos.documento(5L, 3L)));
    assertThat(servicio.crearEnlace(1L, 3L, 1).token())
        .isNotEqualTo(servicio.crearEnlace(1L, 3L, 1).token());
  }

  @Test
  void rechazaVigenciaFueraDeRango() {
    conContrato();
    when(documentos.findByContratoId(3L)).thenReturn(Optional.of(Datos.documento(5L, 3L)));
    assertThatThrownBy(() -> servicio.crearEnlace(1L, 3L, 169))
        .isInstanceOf(ValidacionException.class);
    assertThatThrownBy(() -> servicio.crearEnlace(1L, 3L, 0))
        .isInstanceOf(ValidacionException.class);
  }

  @Test
  void revocaEnlacesVigentes() {
    conContrato();
    when(documentos.findByContratoId(3L)).thenReturn(Optional.of(Datos.documento(5L, 3L)));
    EnlaceCompartido e1 = new EnlaceCompartido(5L, "a", Datos.AHORA.plusSeconds(60), 1L);
    EnlaceCompartido e2 = new EnlaceCompartido(5L, "b", Datos.AHORA.plusSeconds(60), 1L);
    when(enlaces.findByDocumentoIdAndRevocadoFalse(5L)).thenReturn(List.of(e1, e2));
    assertThat(servicio.revocarEnlaces(1L, 3L)).isEqualTo(2);
    assertThat(e1.isRevocado()).isTrue();
    assertThat(e2.isRevocado()).isTrue();
    verify(enlaces).saveAll(anyList());
  }

  @Test
  void descargaPorEnlaceVigente() {
    when(enlaces.findByToken("tok"))
        .thenReturn(Optional.of(new EnlaceCompartido(5L, "tok", Datos.AHORA.plusSeconds(60), 1L)));
    when(documentos.findById(5L)).thenReturn(Optional.of(Datos.documento(5L, 3L)));
    when(contratos.findById(3L)).thenReturn(Optional.of(Datos.contrato(3L, 1L, 1L, Datos.HOY)));
    when(almacenamiento.leer("ruta-5")).thenReturn(Datos.pdf());
    assertThat(servicio.descargarCompartido("tok").contenido()).isEqualTo(Datos.pdf());
    verify(auditoria)
        .registrar(
            eq(1L),
            isNull(),
            eq(AccionAuditoria.DESCARGA_COMPARTIDA),
            eq("documento"),
            eq(5L),
            any());
  }

  @Test
  void enlaceExpiradoDevuelve410() {
    when(enlaces.findByToken("tok"))
        .thenReturn(Optional.of(new EnlaceCompartido(5L, "tok", Datos.AHORA.minusSeconds(1), 1L)));
    assertThatThrownBy(() -> servicio.descargarCompartido("tok"))
        .isInstanceOf(EnlaceExpiradoException.class);
  }

  @Test
  void enlaceInexistente() {
    when(enlaces.findByToken("x")).thenReturn(Optional.empty());
    assertThatThrownBy(() -> servicio.descargarCompartido("x"))
        .isInstanceOf(NoEncontradoException.class);
  }

  @Test
  void limpiaNombresPeligrosos() {
    assertThat(DocumentoService.limpiarNombre("C:\\temp\\..\\<script>.pdf"))
        .isEqualTo("_script_.pdf");
    assertThat(DocumentoService.limpiarNombre("../../etc/passwd.pdf")).isEqualTo("passwd.pdf");
    assertThat(DocumentoService.limpiarNombre(null)).isEqualTo("documento");
    assertThat(DocumentoService.limpiarNombre("  ")).isEqualTo("documento");
    assertThat(DocumentoService.limpiarNombre("a".repeat(250) + ".pdf"))
        .hasSize(200)
        .endsWith(".pdf");
  }

  @Test
  void tipoDeContenidoSegunExtension() {
    assertThat(DocumentoService.tipoContenido("a.doc", TipoDocumento.OFFICE_BINARIO))
        .isEqualTo("application/msword");
    assertThat(DocumentoService.tipoContenido("a.ppt", TipoDocumento.OFFICE_BINARIO))
        .isEqualTo("application/vnd.ms-powerpoint");
    assertThat(DocumentoService.tipoContenido("a.docx", TipoDocumento.OFFICE_XML))
        .contains("wordprocessingml");
    assertThat(DocumentoService.tipoContenido("a.pptx", TipoDocumento.OFFICE_XML))
        .contains("presentationml");
    assertThat(DocumentoService.tipoContenido("a.png", TipoDocumento.PNG)).isEqualTo("image/png");
  }

  @Test
  void calculaSha256() {
    assertThat(DocumentoService.sha256("abc".getBytes(StandardCharsets.US_ASCII)))
        .isEqualTo("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
  }
}
