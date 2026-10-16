package cl.crealab.atlas.domain.service;

import java.util.List;

/** Tipos de documento permitidos por RF-5.2 y su firma binaria (magic bytes). */
public enum TipoDocumento {
  PDF("application/pdf", List.of("pdf")),
  PNG("image/png", List.of("png")),
  JPG("image/jpeg", List.of("jpg", "jpeg")),
  /** Formato binario OLE de Office: .doc y .ppt. */
  OFFICE_BINARIO("application/octet-stream", List.of("doc", "ppt")),
  /** Formato OOXML (zip) de Office: .docx y .pptx. */
  OFFICE_XML("application/octet-stream", List.of("docx", "pptx"));

  private final String tipoContenido;
  private final List<String> extensiones;

  TipoDocumento(String tipoContenido, List<String> extensiones) {
    this.tipoContenido = tipoContenido;
    this.extensiones = extensiones;
  }

  public String getTipoContenido() {
    return tipoContenido;
  }

  public List<String> getExtensiones() {
    return extensiones;
  }
}
