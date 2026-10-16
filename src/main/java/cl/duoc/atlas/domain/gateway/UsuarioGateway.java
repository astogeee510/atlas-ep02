package cl.duoc.atlas.domain.gateway;

import cl.duoc.atlas.domain.model.Usuario;
import java.util.Optional;

/** Puerto de salida de usuarios. */
public interface UsuarioGateway {

  Optional<Usuario> findByNombreUsuario(String nombreUsuario);

  Usuario save(Usuario usuario);
}
