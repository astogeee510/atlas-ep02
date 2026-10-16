package cl.duoc.atlas.infrastructure.adapters.jpa;

import cl.duoc.atlas.domain.model.Cliente;
import org.springframework.stereotype.Component;

/** Traduce entre el modelo de dominio Cliente y la entidad de persistencia. */
@Component
public class ClienteMapper {

  public Cliente toDomain(ClienteData data) {
    if (data == null) {
      return null;
    }
    return new Cliente(
        data.getId(),
        data.getRut(),
        data.getNombre(),
        data.getDescripcion(),
        data.getDireccion(),
        data.getEmail(),
        data.getTelefono(),
        data.isActivo());
  }

  public ClienteData toData(Cliente cliente) {
    if (cliente == null) {
      return null;
    }
    ClienteData data = new ClienteData();
    data.setId(cliente.getId());
    data.setRut(cliente.getRut());
    data.setNombre(cliente.getNombre());
    data.setDescripcion(cliente.getDescripcion());
    data.setDireccion(cliente.getDireccion());
    data.setEmail(cliente.getEmail());
    data.setTelefono(cliente.getTelefono());
    data.setActivo(cliente.isActivo());
    return data;
  }
}
