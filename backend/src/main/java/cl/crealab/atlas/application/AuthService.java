package cl.crealab.atlas.application;

import cl.crealab.atlas.application.dto.LoginRequest;
import cl.crealab.atlas.application.dto.MembresiaResponse;
import cl.crealab.atlas.application.dto.PerfilResponse;
import cl.crealab.atlas.application.dto.RegistroRequest;
import cl.crealab.atlas.application.dto.ResultadoLogin;
import cl.crealab.atlas.application.dto.SesionResponse;
import cl.crealab.atlas.application.dto.UsuarioResponse;
import cl.crealab.atlas.domain.exception.ConflictoException;
import cl.crealab.atlas.domain.exception.CredencialesInvalidasException;
import cl.crealab.atlas.domain.exception.ValidacionException;
import cl.crealab.atlas.domain.model.AccionAuditoria;
import cl.crealab.atlas.domain.model.Empresa;
import cl.crealab.atlas.domain.model.TokenRevocado;
import cl.crealab.atlas.domain.model.Usuario;
import cl.crealab.atlas.domain.service.PoliticaContrasena;
import cl.crealab.atlas.infrastructure.persistence.EmpresaRepository;
import cl.crealab.atlas.infrastructure.persistence.MembresiaRepository;
import cl.crealab.atlas.infrastructure.persistence.TokenRevocadoRepository;
import cl.crealab.atlas.infrastructure.persistence.UsuarioRepository;
import cl.crealab.atlas.infrastructure.security.JwtService;
import cl.crealab.atlas.infrastructure.security.LimitadorIntentos;
import cl.crealab.atlas.infrastructure.security.TokenEmitido;
import cl.crealab.atlas.infrastructure.security.UsuarioAutenticado;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Registro, autenticacion y cierre de sesion (RF-1, RF-2.1). */
@Service
public class AuthService {

  private static final Logger LOG = LoggerFactory.getLogger(AuthService.class);

  /** Hash ficticio para igualar el tiempo de respuesta cuando el usuario no existe. */
  private static final String HASH_FICTICIO =
      "$2a$12$iDN8Ass3X5xi2rtY8JEG.uVF2T88dbss9CFFy71l54rtGPuaNS.ga";

  private final UsuarioRepository usuarios;
  private final MembresiaRepository membresias;
  private final EmpresaRepository empresas;
  private final TokenRevocadoRepository revocados;
  private final PasswordEncoder encoder;
  private final JwtService jwtService;
  private final LimitadorIntentos limitador;
  private final PoliticaContrasena politica;
  private final AuditoriaService auditoria;

  public AuthService(
      UsuarioRepository usuarios,
      MembresiaRepository membresias,
      EmpresaRepository empresas,
      TokenRevocadoRepository revocados,
      PasswordEncoder encoder,
      JwtService jwtService,
      LimitadorIntentos limitador,
      PoliticaContrasena politica,
      AuditoriaService auditoria) {
    this.usuarios = usuarios;
    this.membresias = membresias;
    this.empresas = empresas;
    this.revocados = revocados;
    this.encoder = encoder;
    this.jwtService = jwtService;
    this.limitador = limitador;
    this.politica = politica;
    this.auditoria = auditoria;
  }

  /**
   * Registra un usuario nuevo (RF-2.1).
   *
   * @param solicitud datos de registro
   * @return usuario creado
   */
  @Transactional
  public UsuarioResponse registrar(RegistroRequest solicitud) {
    List<String> errores = politica.validar(solicitud.contrasena());
    if (!errores.isEmpty()) {
      throw new ValidacionException("La contrasena no cumple la politica", errores);
    }
    if (usuarios.existsByNombreUsuario(solicitud.nombreUsuario())) {
      throw new ConflictoException("El nombre de usuario ya existe");
    }
    String correo = solicitud.correo().trim().toLowerCase(Locale.ROOT);
    if (usuarios.existsByCorreoIgnoreCase(correo)) {
      throw new ConflictoException("El correo ya esta registrado");
    }
    Usuario usuario =
        usuarios.save(
            new Usuario(
                solicitud.nombreUsuario(),
                correo,
                encoder.encode(solicitud.contrasena()),
                solicitud.consentimiento()));
    auditoria.registrar(
        null,
        usuario.getId(),
        AccionAuditoria.REGISTRAR_USUARIO,
        "usuario",
        usuario.getId(),
        "Registro con consentimiento de datos");
    return UsuarioResponse.desde(usuario);
  }

  /**
   * Autentica al usuario (RF-1.1) con proteccion de fuerza bruta.
   *
   * @param solicitud credenciales
   * @param ip direccion IP de origen
   * @return sesion y token de refresco
   */
  @Transactional(readOnly = true)
  public ResultadoLogin login(LoginRequest solicitud, String ip) {
    String clave = solicitud.nombreUsuario().toLowerCase(Locale.ROOT) + "|" + ip;
    limitador.verificar(clave);
    Optional<Usuario> encontrado =
        usuarios.findByNombreUsuario(solicitud.nombreUsuario()).filter(Usuario::isActivo);
    String hash = encontrado.map(Usuario::getPasswordHash).orElse(HASH_FICTICIO);
    boolean coincide = encoder.matches(solicitud.contrasena(), hash);
    if (encontrado.isEmpty() || !coincide) {
      limitador.registrarFallo(clave);
      LOG.warn("Login fallido para usuario={}", sanitizar(solicitud.nombreUsuario()));
      throw new CredencialesInvalidasException("Usuario o contrasena incorrectos");
    }
    limitador.limpiar(clave);
    Usuario usuario = encontrado.get();
    TokenEmitido acceso =
        jwtService.emitir(usuario.getId(), usuario.getNombreUsuario(), JwtService.TIPO_ACCESO);
    TokenEmitido refresco =
        jwtService.emitir(usuario.getId(), usuario.getNombreUsuario(), JwtService.TIPO_REFRESCO);
    return new ResultadoLogin(sesion(usuario, acceso), refresco);
  }

  /**
   * Emite un nuevo token de acceso a partir del token de refresco (cookie).
   *
   * @param tokenRefresco token de refresco
   * @return nueva sesion
   */
  @Transactional(readOnly = true)
  public SesionResponse refrescar(String tokenRefresco) {
    UsuarioAutenticado datos =
        jwtService
            .validar(tokenRefresco, JwtService.TIPO_REFRESCO)
            .filter(d -> !revocados.existsById(d.jti()))
            .orElseThrow(() -> new CredencialesInvalidasException("La sesion expiro"));
    Usuario usuario =
        usuarios
            .findById(datos.id())
            .filter(Usuario::isActivo)
            .orElseThrow(() -> new CredencialesInvalidasException("La sesion expiro"));
    TokenEmitido acceso =
        jwtService.emitir(usuario.getId(), usuario.getNombreUsuario(), JwtService.TIPO_ACCESO);
    return sesion(usuario, acceso);
  }

  /**
   * Cierra la sesion revocando el token de acceso y el de refresco (RF-1.2).
   *
   * @param actual usuario autenticado (con el jti del token de acceso)
   * @param tokenRefresco token de refresco de la cookie (puede ser null)
   */
  @Transactional
  public void logout(UsuarioAutenticado actual, String tokenRefresco) {
    revocados.save(new TokenRevocado(actual.jti(), actual.expiraEn()));
    jwtService
        .validar(tokenRefresco, JwtService.TIPO_REFRESCO)
        .ifPresent(d -> revocados.save(new TokenRevocado(d.jti(), d.expiraEn())));
  }

  /**
   * Devuelve el perfil del usuario autenticado.
   *
   * @param usuarioId usuario
   * @return perfil con sus empresas
   */
  @Transactional(readOnly = true)
  public PerfilResponse perfil(Long usuarioId) {
    Usuario usuario =
        usuarios
            .findById(usuarioId)
            .orElseThrow(() -> new CredencialesInvalidasException("Usuario no encontrado"));
    return new PerfilResponse(UsuarioResponse.desde(usuario), empresasDe(usuarioId));
  }

  /**
   * Empresas del usuario con su rol.
   *
   * @param usuarioId usuario
   * @return lista de membresias
   */
  public List<MembresiaResponse> empresasDe(Long usuarioId) {
    return membresias.findByUsuarioId(usuarioId).stream()
        .map(
            m ->
                new MembresiaResponse(
                    m.getEmpresaId(),
                    empresas.findById(m.getEmpresaId()).map(Empresa::getNombre).orElse(""),
                    m.getRol()))
        .toList();
  }

  private SesionResponse sesion(Usuario usuario, TokenEmitido acceso) {
    return new SesionResponse(
        acceso.token(),
        "Bearer",
        jwtService.getDuracionAcceso().toSeconds(),
        UsuarioResponse.desde(usuario),
        empresasDe(usuario.getId()));
  }

  /** Evita inyeccion de saltos de linea en el log (log forging). */
  static String sanitizar(String valor) {
    return valor == null ? "" : valor.replaceAll("[\\r\\n\\t]", "_");
  }
}
