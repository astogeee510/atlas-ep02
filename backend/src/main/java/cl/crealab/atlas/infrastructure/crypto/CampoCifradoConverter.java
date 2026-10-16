package cl.crealab.atlas.infrastructure.crypto;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** Convierte de forma transparente columnas sensibles a texto cifrado AES-GCM (NFR-SEG-3). */
@Converter
public class CampoCifradoConverter implements AttributeConverter<String, String> {

  private final AesGcmCifrador cifrador;

  public CampoCifradoConverter() {
    this.cifrador = AesGcmCifrador.desdeEntorno();
  }

  @Override
  public String convertToDatabaseColumn(String atributo) {
    return atributo == null ? null : cifrador.cifrar(atributo);
  }

  @Override
  public String convertToEntityAttribute(String columna) {
    return columna == null ? null : cifrador.descifrar(columna);
  }
}
