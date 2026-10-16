package cl.duoc.atlas.infrastructure.adapters.jpa;

import cl.duoc.atlas.domain.model.Usuario;
import org.springframework.stereotype.Component;

/** Traduce entre el modelo de dominio Usuario y la entidad de persistencia. */
@Component
public class UsuarioMapper {

  public Usuario toDomain(UsuarioData data) {
    if (data == null) {
      return null;
    }
    return new Usuario(
        data.getId(),
        data.getNombreUsuario(),
        data.getHashContrasena(),
        data.getRol(),
        data.isActivo());
  }

  public UsuarioData toData(Usuario usuario) {
    if (usuario == null) {
      return null;
    }
    UsuarioData data = new UsuarioData();
    data.setId(usuario.getId());
    data.setNombreUsuario(usuario.getNombreUsuario());
    data.setHashContrasena(usuario.getHashContrasena());
    data.setRol(usuario.getRol());
    data.setActivo(usuario.isActivo());
    return data;
  }
}
