package cl.crealab.atlas.application;

import cl.crealab.atlas.domain.exception.AccesoDenegadoException;
import cl.crealab.atlas.domain.model.Membresia;
import cl.crealab.atlas.domain.model.Rol;
import cl.crealab.atlas.infrastructure.persistence.MembresiaRepository;
import org.springframework.stereotype.Service;

/**
 * Control de acceso en el backend (NFR-SEG-4). Aplica RB-2 (solo miembros de la empresa), RB-4 (el
 * editor solo lee) y RB-5 (el administrador escribe).
 */
@Service
public class ControlAcceso {

  private final MembresiaRepository membresias;

  public ControlAcceso(MembresiaRepository membresias) {
    this.membresias = membresias;
  }

  /**
   * Exige que el usuario pertenezca a la empresa.
   *
   * @param usuarioId usuario
   * @param empresaId empresa
   * @return el rol del usuario en la empresa
   */
  public Rol exigirMiembro(Long usuarioId, Long empresaId) {
    return membresias
        .findByEmpresaIdAndUsuarioId(empresaId, usuarioId)
        .map(Membresia::getRol)
        .orElseThrow(() -> new AccesoDenegadoException("No tiene acceso a esta empresa"));
  }

  /**
   * Exige que el usuario sea administrador de la empresa.
   *
   * @param usuarioId usuario
   * @param empresaId empresa
   */
  public void exigirAdmin(Long usuarioId, Long empresaId) {
    if (exigirMiembro(usuarioId, empresaId) != Rol.ADMIN) {
      throw new AccesoDenegadoException("La operacion requiere rol administrador");
    }
  }
}
