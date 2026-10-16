package cl.crealab.atlas.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

/** CP-SEG-05: validacion de archivos por firma binaria (RF-5.2, NFR-SEG-5). */
class DetectorTipoDocumentoTest {

  private static final byte[] PDF = "%PDF-1.7".getBytes(StandardCharsets.US_ASCII);
  private static final byte[] PNG = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0};
  private static final byte[] JPG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0};
  private static final byte[] OLE = {
    (byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0, (byte) 0xA1, (byte) 0xB1, 0x1A, (byte) 0xE1
  };
  private static final byte[] ZIP = {0x50, 0x4B, 0x03, 0x04, 0x14};
  private static final byte[] EXE = {0x4D, 0x5A, (byte) 0x90, 0x00};

  @Test
  void detectaCadaTipoPermitido() {
    assertThat(DetectorTipoDocumento.detectar(PDF, "a.pdf")).contains(TipoDocumento.PDF);
    assertThat(DetectorTipoDocumento.detectar(PNG, "a.PNG")).contains(TipoDocumento.PNG);
    assertThat(DetectorTipoDocumento.detectar(JPG, "a.jpeg")).contains(TipoDocumento.JPG);
    assertThat(DetectorTipoDocumento.detectar(OLE, "a.doc")).contains(TipoDocumento.OFFICE_BINARIO);
    assertThat(DetectorTipoDocumento.detectar(OLE, "a.ppt")).contains(TipoDocumento.OFFICE_BINARIO);
    assertThat(DetectorTipoDocumento.detectar(ZIP, "a.docx")).contains(TipoDocumento.OFFICE_XML);
    assertThat(DetectorTipoDocumento.detectar(ZIP, "a.pptx")).contains(TipoDocumento.OFFICE_XML);
  }

  @Test
  void rechazaEjecutableRenombradoComoPdf() {
    assertThat(DetectorTipoDocumento.detectar(EXE, "factura.pdf")).isEmpty();
  }

  @Test
  void rechazaExtensionQueNoCoincideConLaFirma() {
    assertThat(DetectorTipoDocumento.detectar(PDF, "imagen.png")).isEmpty();
    assertThat(DetectorTipoDocumento.detectar(PNG, "sin-extension")).isEmpty();
  }

  @Test
  void rechazaArchivoMasCortoQueLaFirma() {
    assertThat(DetectorTipoDocumento.detectarPorFirma(new byte[] {0x25})).isEmpty();
    assertThat(DetectorTipoDocumento.detectarPorFirma(null)).isEmpty();
  }

  @Test
  void obtieneExtension() {
    assertThat(DetectorTipoDocumento.extension("Contrato.Final.PDF")).isEqualTo("pdf");
    assertThat(DetectorTipoDocumento.extension("archivo.")).isEmpty();
    assertThat(DetectorTipoDocumento.extension("archivo")).isEmpty();
    assertThat(DetectorTipoDocumento.extension(null)).isEmpty();
  }

  @Test
  void tiposExponenExtensiones() {
    assertThat(TipoDocumento.PDF.getExtensiones()).containsExactly("pdf");
    assertThat(TipoDocumento.PNG.getTipoContenido()).isEqualTo("image/png");
  }
}
