package cl.crealab.atlas.application;

import cl.crealab.atlas.application.dto.NuevoUsuarioRequest;
import cl.crealab.atlas.application.dto.UsuarioEmpresaResponse;
import cl.crealab.atlas.domain.exception.ConflictoException;
import cl.crealab.atlas.domain.exception.NoEncontradoException;
import cl.crealab.atlas.domain.exception.ValidacionException;
import cl.crealab.atlas.domain.model.AccionAuditoria;
import cl.crealab.atlas.domain.model.Membresia;
import cl.crealab.atlas.domain.model.Rol;
import cl.crealab.atlas.domain.model.Usuario;
import cl.crealab.atlas.domain.service.PoliticaContrasena;
import cl.crealab.atlas.infrastructure.persistence.MembresiaRepository;
import cl.crealab.atlas.infrastructure.persistence.UsuarioRepository;
import java.util.List;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Gestion de usuarios de la empresa y sus roles (RF-3.1, RF-3.3). Solo administradores. */
@Service
public class UsuarioEmpresaService {

  private final UsuarioRepository usuarios;
  private final MembresiaRepository membresias;
  private final ControlAcceso acceso;
  private final PasswordEncoder encoder;
  private final PoliticaContrasena politica;
  private final AuditoriaService auditoria;

  public UsuarioEmpresaService(
      UsuarioRepository usuarios,
      MembresiaRepository membresias,
      ControlAcceso acceso,
      PasswordEncoder encoder,
      PoliticaContrasena politica,
      AuditoriaService auditoria) {
    this.usuarios = usuarios;
    this.membresias = membresias;
    this.acceso = acceso;
    this.encoder = encoder;
    this.politica = politica;
    this.auditoria = auditoria;
  }

  /**
   * Lista los usuarios de la empresa.
   *
   * @param actorId administrador que consulta
   * @param empresaId empresa
   * @return usuarios con su rol
   */
  @Transactional(readOnly = true)
  public List<UsuarioEmpresaResponse> listar(Long actorId, Long empresaId) {
    acceso.exigirAdmin(actorId, empresaId);
    return membresias.findByEmpresaId(empresaId).stream().map(this::respuesta).toList();
  }

  /**
   * Agrega un usuario a la empresa: lo invita si ya existe o lo crea si es nuevo (RF-3.1).
   *
   * @param actorId administrador
   * @param empresaId empresa
   * @param solicitud datos del usuario y rol
   * @return usuario agregado
   */
  @Transactional
  public UsuarioEmpresaResponse agregar(
      Long actorId, Long empresaId, NuevoUsuarioRequest solicitud) {
    acceso.exigirAdmin(actorId, empresaId);
    Usuario usuario =
        usuarios
            .findByNombreUsuario(solicitud.nombreUsuario())
            .orElseGet(() -> crearUsuario(solicitud));
    if (membresias.findByEmpresaIdAndUsuarioId(empresaId, usuario.getId()).isPresent()) {
      throw new ConflictoException("El usuario ya pertenece a la empresa");
    }
    Membresia membresia =
        membresias.save(new Membresia(empresaId, usuario.getId(), solicitud.rol()));
    auditoria.registrar(
        empresaId,
        actorId,
        AccionAuditoria.CREAR_USUARIO,
        "usuario",
        usuario.getId(),
        "Rol " + solicitud.rol());
    return new UsuarioEmpresaResponse(
        usuario.getId(), usuario.getNombreUsuario(), usuario.getCorreo(), membresia.getRol());
  }

  /**
   * Cambia el rol de un usuario (RF-3.3). La empresa siempre conserva al menos un administrador.
   *
   * @param actorId administrador
   * @param empresaId empresa
   * @param usuarioId usuario a modificar
   * @param rol nuevo rol
   * @return usuario actualizado
   */
  @Transactional
  public UsuarioEmpresaResponse cambiarRol(Long actorId, Long empresaId, Long usuarioId, Rol rol) {
    acceso.exigirAdmin(actorId, empresaId);
    Membresia membresia = buscar(empresaId, usuarioId);
    if (membresia.getRol() == Rol.ADMIN && rol != Rol.ADMIN) {
      exigirOtroAdmin(empresaId);
    }
    membresia.setRol(rol);
    membresias.save(membresia);
    auditoria.registrar(
        empresaId, actorId, AccionAuditoria.EDITAR_USUARIO, "usuario", usuarioId, "Rol " + rol);
    return respuesta(membresia);
  }

  /**
   * Quita un usuario de la empresa (RF-3.3).
   *
   * @param actorId administrador
   * @param empresaId empresa
   * @param usuarioId usuario a quitar
   */
  @Transactional
  public void eliminar(Long actorId, Long empresaId, Long usuarioId) {
    acceso.exigirAdmin(actorId, empresaId);
    Membresia membresia = buscar(empresaId, usuarioId);
    if (membresia.getRol() == Rol.ADMIN) {
      exigirOtroAdmin(empresaId);
    }
    membresias.delete(membresia);
    auditoria.registrar(
        empresaId,
        actorId,
        AccionAuditoria.ELIMINAR_USUARIO,
        "usuario",
        usuarioId,
        "Quitado de la empresa");
  }

  private void exigirOtroAdmin(Long empresaId) {
    if (membresias.countByEmpresaIdAndRol(empresaId, Rol.ADMIN) <= 1) {
      throw new ConflictoException("La empresa debe conservar al menos un administrador");
    }
  }

  private Membresia buscar(Long empresaId, Long usuarioId) {
    return membresias
        .findByEmpresaIdAndUsuarioId(empresaId, usuarioId)
        .orElseThrow(() -> new NoEncontradoException("El usuario no pertenece a la empresa"));
  }

  private Usuario crearUsuario(NuevoUsuarioRequest solicitud) {
    if (solicitud.correo() == null || solicitud.contrasena() == null) {
      throw new ValidacionException("Para un usuario nuevo se requieren correo y contrasena");
    }
    List<String> errores = politica.validar(solicitud.contrasena());
    if (!errores.isEmpty()) {
      throw new ValidacionException("La contrasena no cumple la politica", errores);
    }
    String correo = solicitud.correo().trim().toLowerCase(Locale.ROOT);
    if (usuarios.existsByCorreoIgnoreCase(correo)) {
      throw new ConflictoException("El correo ya esta registrado");
    }
    return usuarios.save(
        new Usuario(
            solicitud.nombreUsuario(), correo, encoder.encode(solicitud.contrasena()), false));
  }

  private UsuarioEmpresaResponse respuesta(Membresia membresia) {
    Usuario usuario =
        usuarios
            .findById(membresia.getUsuarioId())
            .orElseThrow(() -> new NoEncontradoException("Usuario no encontrado"));
    return new UsuarioEmpresaResponse(
        usuario.getId(), usuario.getNombreUsuario(), usuario.getCorreo(), membresia.getRol());
  }
}
