package cl.crealab.atlas.application;

import cl.crealab.atlas.application.dto.ArchivoDescarga;
import cl.crealab.atlas.application.dto.DocumentoResponse;
import cl.crealab.atlas.application.dto.EnlaceResponse;
import cl.crealab.atlas.domain.exception.ArchivoNoPermitidoException;
import cl.crealab.atlas.domain.exception.EnlaceExpiradoException;
import cl.crealab.atlas.domain.exception.NoEncontradoException;
import cl.crealab.atlas.domain.exception.ValidacionException;
import cl.crealab.atlas.domain.model.AccionAuditoria;
import cl.crealab.atlas.domain.model.Contrato;
import cl.crealab.atlas.domain.model.Documento;
import cl.crealab.atlas.domain.model.EnlaceCompartido;
import cl.crealab.atlas.domain.service.DetectorTipoDocumento;
import cl.crealab.atlas.domain.service.TipoDocumento;
import cl.crealab.atlas.infrastructure.persistence.ContratoRepository;
import cl.crealab.atlas.infrastructure.persistence.DocumentoRepository;
import cl.crealab.atlas.infrastructure.persistence.EnlaceCompartidoRepository;
import cl.crealab.atlas.infrastructure.storage.AlmacenamientoArchivos;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Documentos de contratos (RF-5.2, RF-5.3, RF-5.5): un documento por contrato, validado por su
 * firma binaria, guardado en storage interno y compartible mediante enlaces temporales.
 */
@Service
public class DocumentoService {

  private static final int HORAS_ENLACE_DEFECTO = 24;
  private static final int HORAS_ENLACE_MAXIMO = 168;
  private static final int BYTES_TOKEN = 32;
  private static final int LARGO_MAXIMO_NOMBRE = 200;
  private static final SecureRandom ALEATORIO = new SecureRandom();

  private final ContratoRepository contratos;
  private final DocumentoRepository documentos;
  private final EnlaceCompartidoRepository enlaces;
  private final AlmacenamientoArchivos almacenamiento;
  private final ControlAcceso acceso;
  private final AuditoriaService auditoria;
  private final Clock clock;
  private final long maxBytes;

  public DocumentoService(
      ContratoRepository contratos,
      DocumentoRepository documentos,
      EnlaceCompartidoRepository enlaces,
      AlmacenamientoArchivos almacenamiento,
      ControlAcceso acceso,
      AuditoriaService auditoria,
      Clock clock,
      @Value("${atlas.documentos.max-bytes}") long maxBytes) {
    this.contratos = contratos;
    this.documentos = documentos;
    this.enlaces = enlaces;
    this.almacenamiento = almacenamiento;
    this.acceso = acceso;
    this.auditoria = auditoria;
    this.clock = clock;
    this.maxBytes = maxBytes;
  }

  /**
   * Sube (o reemplaza) el documento de un contrato.
   *
   * @param actorId administrador
   * @param contratoId contrato
   * @param nombreOriginal nombre del archivo enviado
   * @param contenido bytes del archivo
   * @return metadatos del documento guardado
   */
  @Transactional
  public DocumentoResponse subir(
      Long actorId, Long contratoId, String nombreOriginal, byte[] contenido) {
    Contrato contrato = buscarContrato(contratoId);
    acceso.exigirAdmin(actorId, contrato.getEmpresaId());
    if (contenido == null || contenido.length == 0) {
      throw new ValidacionException("El archivo esta vacio");
    }
    if (contenido.length > maxBytes) {
      throw new ValidacionException("El archivo supera el tamano maximo de 10 MB");
    }
    String nombre = limpiarNombre(nombreOriginal);
    TipoDocumento tipo =
        DetectorTipoDocumento.detectar(contenido, nombre)
            .orElseThrow(
                () ->
                    new ArchivoNoPermitidoException(
                        "Tipo de archivo no permitido. Use PDF, DOC, DOCX, PPT, PPTX, JPG o PNG"));
    documentos
        .findByContratoId(contratoId)
        .ifPresent(
            anterior -> {
              almacenamiento.eliminar(anterior.getRutaAlmacenamiento());
              documentos.delete(anterior);
              documentos.flush();
            });
    Documento documento = new Documento(contratoId, nombre, tipoContenido(nombre, tipo), actorId);
    documento.setTamanoBytes(contenido.length);
    documento.setRutaAlmacenamiento(almacenamiento.guardar(contenido));
    documento.setSha256(sha256(contenido));
    documento = documentos.save(documento);
    auditoria.registrar(
        contrato.getEmpresaId(),
        actorId,
        AccionAuditoria.SUBIR_DOCUMENTO,
        "documento",
        documento.getId(),
        nombre);
    return DocumentoResponse.desde(documento);
  }

  /**
   * Metadatos del documento de un contrato.
   *
   * @param actorId usuario
   * @param contratoId contrato
   * @return metadatos
   */
  @Transactional(readOnly = true)
  public DocumentoResponse obtener(Long actorId, Long contratoId) {
    Contrato contrato = buscarContrato(contratoId);
    acceso.exigirMiembro(actorId, contrato.getEmpresaId());
    return DocumentoResponse.desde(buscarDocumento(contratoId));
  }

  /**
   * Descarga el documento de un contrato (queda auditada).
   *
   * @param actorId usuario
   * @param contratoId contrato
   * @return archivo
   */
  @Transactional
  public ArchivoDescarga descargar(Long actorId, Long contratoId) {
    Contrato contrato = buscarContrato(contratoId);
    acceso.exigirMiembro(actorId, contrato.getEmpresaId());
    Documento documento = buscarDocumento(contratoId);
    byte[] contenido = almacenamiento.leer(documento.getRutaAlmacenamiento());
    auditoria.registrar(
        contrato.getEmpresaId(),
        actorId,
        AccionAuditoria.DESCARGAR_DOCUMENTO,
        "documento",
        documento.getId(),
        documento.getNombreOriginal());
    return new ArchivoDescarga(
        documento.getNombreOriginal(), documento.getTipoContenido(), contenido);
  }

  /**
   * Genera un enlace temporal para compartir el documento (RF-5.5).
   *
   * @param actorId administrador
   * @param contratoId contrato
   * @param horas vigencia (1 a 168; null = 24)
   * @return enlace generado
   */
  @Transactional
  public EnlaceResponse crearEnlace(Long actorId, Long contratoId, Integer horas) {
    Contrato contrato = buscarContrato(contratoId);
    acceso.exigirAdmin(actorId, contrato.getEmpresaId());
    Documento documento = buscarDocumento(contratoId);
    int vigencia = horas == null ? HORAS_ENLACE_DEFECTO : horas;
    if (vigencia < 1 || vigencia > HORAS_ENLACE_MAXIMO) {
      throw new ValidacionException("La vigencia debe estar entre 1 y 168 horas");
    }
    String token = generarToken();
    Instant expira = clock.instant().plus(Duration.ofHours(vigencia));
    enlaces.save(new EnlaceCompartido(documento.getId(), token, expira, actorId));
    auditoria.registrar(
        contrato.getEmpresaId(),
        actorId,
        AccionAuditoria.COMPARTIR_DOCUMENTO,
        "documento",
        documento.getId(),
        "Enlace por " + vigencia + " h");
    return new EnlaceResponse(token, "/api/compartido/" + token, expira);
  }

  /**
   * Revoca todos los enlaces vigentes del documento de un contrato.
   *
   * @param actorId administrador
   * @param contratoId contrato
   * @return cantidad de enlaces revocados
   */
  @Transactional
  public int revocarEnlaces(Long actorId, Long contratoId) {
    Contrato contrato = buscarContrato(contratoId);
    acceso.exigirAdmin(actorId, contrato.getEmpresaId());
    Documento documento = buscarDocumento(contratoId);
    List<EnlaceCompartido> vigentes = enlaces.findByDocumentoIdAndRevocadoFalse(documento.getId());
    vigentes.forEach(e -> e.setRevocado(true));
    enlaces.saveAll(vigentes);
    auditoria.registrar(
        contrato.getEmpresaId(),
        actorId,
        AccionAuditoria.REVOCAR_ENLACE,
        "documento",
        documento.getId(),
        vigentes.size() + " enlaces revocados");
    return vigentes.size();
  }

  /**
   * Descarga un documento mediante un enlace compartido vigente.
   *
   * @param token token del enlace
   * @return archivo
   */
  @Transactional
  public ArchivoDescarga descargarCompartido(String token) {
    EnlaceCompartido enlace =
        enlaces.findByToken(token).orElseThrow(() -> new NoEncontradoException("Enlace no valido"));
    if (!enlace.esVigente(clock.instant())) {
      throw new EnlaceExpiradoException("El enlace expiro o fue revocado");
    }
    Documento documento =
        documentos
            .findById(enlace.getDocumentoId())
            .orElseThrow(() -> new NoEncontradoException("Documento no encontrado"));
    Long empresaId =
        contratos.findById(documento.getContratoId()).map(Contrato::getEmpresaId).orElse(null);
    byte[] contenido = almacenamiento.leer(documento.getRutaAlmacenamiento());
    auditoria.registrar(
        empresaId,
        null,
        AccionAuditoria.DESCARGA_COMPARTIDA,
        "documento",
        documento.getId(),
        "Descarga por enlace compartido");
    return new ArchivoDescarga(
        documento.getNombreOriginal(), documento.getTipoContenido(), contenido);
  }

  private Contrato buscarContrato(Long contratoId) {
    return contratos
        .findByIdAndEliminadoFalse(contratoId)
        .orElseThrow(() -> new NoEncontradoException("Contrato no encontrado"));
  }

  private Documento buscarDocumento(Long contratoId) {
    return documentos
        .findByContratoId(contratoId)
        .orElseThrow(() -> new NoEncontradoException("El contrato no tiene documento"));
  }

  /** Quita rutas y caracteres peligrosos del nombre original. */
  static String limpiarNombre(String nombreOriginal) {
    if (nombreOriginal == null || nombreOriginal.isBlank()) {
      return "documento";
    }
    String nombre = nombreOriginal.replace('\\', '/');
    nombre = nombre.substring(nombre.lastIndexOf('/') + 1);
    nombre = nombre.replaceAll("[^\\p{L}\\p{N}._ -]", "_");
    if (nombre.length() > LARGO_MAXIMO_NOMBRE) {
      nombre = nombre.substring(nombre.length() - LARGO_MAXIMO_NOMBRE);
    }
    return nombre.isBlank() ? "documento" : nombre;
  }

  /** Tipo MIME segun la extension validada. */
  static String tipoContenido(String nombre, TipoDocumento tipo) {
    return switch (DetectorTipoDocumento.extension(nombre)) {
      case "doc" -> "application/msword";
      case "ppt" -> "application/vnd.ms-powerpoint";
      case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
      case "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation";
      default -> tipo.getTipoContenido();
    };
  }

  /** Huella SHA-256 en hexadecimal. */
  static String sha256(byte[] contenido) {
    try {
      return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(contenido));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 no disponible", e);
    }
  }

  private static String generarToken() {
    byte[] bytes = new byte[BYTES_TOKEN];
    ALEATORIO.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }
}
