package cl.duoc.atlas.infrastructure.adapters.jpa;

import cl.duoc.atlas.domain.model.Contrato;
import org.springframework.stereotype.Component;

/** Traduce entre el modelo de dominio Contrato y la entidad de persistencia. */
@Component
public class ContratoMapper {

  public Contrato toDomain(ContratoData data) {
    if (data == null) {
      return null;
    }
    return new Contrato(
        data.getId(),
        data.getClienteId(),
        data.getTitulo(),
        data.getDescripcion(),
        data.getFechaInicio(),
        data.getFechaTermino());
  }

  public ContratoData toData(Contrato contrato) {
    if (contrato == null) {
      return null;
    }
    ContratoData data = new ContratoData();
    data.setId(contrato.getId());
    data.setClienteId(contrato.getClienteId());
    data.setTitulo(contrato.getTitulo());
    data.setDescripcion(contrato.getDescripcion());
    data.setFechaInicio(contrato.getFechaInicio());
    data.setFechaTermino(contrato.getFechaTermino());
    return data;
  }
}
