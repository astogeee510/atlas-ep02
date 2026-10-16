package cl.crealab.atlas.infrastructure.rest;

import static org.assertj.core.api.Assertions.assertThat;

import cl.crealab.atlas.application.dto.ArchivoDescarga;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

/** RF-5.2: descarga como adjunto con nombre codificado (UTF-8). */
class DocumentoControllerTest {

  @Test
  void descargaComoAdjuntoConNombreCodificado() {
    ResponseEntity<byte[]> r =
        DocumentoController.archivo(
            new ArchivoDescarga("contrato ñ.pdf", "application/pdf", new byte[] {1}));
    String disposicion = r.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION);
    assertThat(disposicion).startsWith("attachment");
    assertThat(disposicion).contains("filename");
    assertThat(r.getHeaders().getContentType().toString()).isEqualTo("application/pdf");
    assertThat(r.getBody()).containsExactly(1);
  }
}
