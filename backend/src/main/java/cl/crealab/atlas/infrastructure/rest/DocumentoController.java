package cl.crealab.atlas.infrastructure.rest;

import cl.crealab.atlas.application.DocumentoService;
import cl.crealab.atlas.application.dto.ArchivoDescarga;
import cl.crealab.atlas.application.dto.DocumentoResponse;
import cl.crealab.atlas.application.dto.EnlaceRequest;
import cl.crealab.atlas.application.dto.EnlaceResponse;
import cl.crealab.atlas.domain.exception.ValidacionException;
import cl.crealab.atlas.infrastructure.security.UsuarioAutenticado;
import jakarta.validation.Valid;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** Documentos de contratos y enlaces compartidos (RF-5.2, RF-5.3, RF-5.5). */
@RestController
public class DocumentoController {

  private final DocumentoService servicio;

  public DocumentoController(DocumentoService servicio) {
    this.servicio = servicio;
  }

  @PostMapping(
      path = "/api/contratos/{contratoId}/documentos",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public DocumentoResponse subir(
      @AuthenticationPrincipal UsuarioAutenticado usuario,
      @PathVariable Long contratoId,
      @RequestParam("archivo") MultipartFile archivo) {
    try {
      return servicio.subir(
          usuario.id(), contratoId, archivo.getOriginalFilename(), archivo.getBytes());
    } catch (IOException e) {
      throw new ValidacionException("No fue posible leer el archivo enviado");
    }
  }

  @GetMapping("/api/contratos/{contratoId}/documentos")
  public DocumentoResponse obtener(
      @AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable Long contratoId) {
    return servicio.obtener(usuario.id(), contratoId);
  }

  @GetMapping("/api/contratos/{contratoId}/documentos/descarga")
  public ResponseEntity<byte[]> descargar(
      @AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable Long contratoId) {
    return archivo(servicio.descargar(usuario.id(), contratoId));
  }

  @PostMapping("/api/contratos/{contratoId}/documentos/enlace")
  @ResponseStatus(HttpStatus.CREATED)
  public EnlaceResponse crearEnlace(
      @AuthenticationPrincipal UsuarioAutenticado usuario,
      @PathVariable Long contratoId,
      @Valid @RequestBody(required = false) EnlaceRequest solicitud) {
    Integer horas = solicitud == null ? null : solicitud.horas();
    return servicio.crearEnlace(usuario.id(), contratoId, horas);
  }

  @DeleteMapping("/api/contratos/{contratoId}/documentos/enlace")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void revocarEnlaces(
      @AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable Long contratoId) {
    servicio.revocarEnlaces(usuario.id(), contratoId);
  }

  @GetMapping("/api/compartido/{token}")
  public ResponseEntity<byte[]> descargarCompartido(@PathVariable String token) {
    return archivo(servicio.descargarCompartido(token));
  }

  static ResponseEntity<byte[]> archivo(ArchivoDescarga descarga) {
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(descarga.tipoContenido()))
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            ContentDisposition.attachment()
                .filename(descarga.nombre(), StandardCharsets.UTF_8)
                .build()
                .toString())
        .body(descarga.contenido());
  }
}
