package cl.duoc.atlas.domain.gateway;

import cl.duoc.atlas.domain.model.Contrato;
import java.util.List;
import java.util.Optional;

/** Puerto de salida de contratos. */
public interface ContratoGateway {

  Optional<Contrato> findById(Long id);

  List<Contrato> findByClienteId(Long clienteId);

  Contrato save(Contrato contrato);

  void deleteById(Long id);
}
