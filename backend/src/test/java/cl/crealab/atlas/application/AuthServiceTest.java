package cl.crealab.atlas.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.crealab.atlas.application.dto.LoginRequest;
import cl.crealab.atlas.application.dto.PerfilResponse;
import cl.crealab.atlas.application.dto.RegistroRequest;
import cl.crealab.atlas.application.dto.ResultadoLogin;
import cl.crealab.atlas.application.dto.SesionResponse;
import cl.crealab.atlas.application.dto.UsuarioResponse;
import cl.crealab.atlas.domain.exception.ConflictoException;
import cl.crealab.atlas.domain.exception.CredencialesInvalidasException;
import cl.crealab.atlas.domain.exception.DemasiadosIntentosException;
import cl.crealab.atlas.domain.exception.ValidacionException;
import cl.crealab.atlas.domain.model.AccionAuditoria;
import cl.crealab.atlas.domain.model.Membresia;
import cl.crealab.atlas.domain.model.Rol;
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
import cl.crealab.atlas.soporte.Datos;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

/** CP-FUN-01 / CP-FUN-02: registro, login, refresco y logout (RF-1, RF-2.1). */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  private static final String CLAVE = "Atlas#Demo2026";

  @Mock private UsuarioRepository usuarios;
  @Mock private MembresiaRepository membresias;
  @Mock private EmpresaRepository empresas;
  @Mock private TokenRevocadoRepository revocados;
  @Mock private PasswordEncoder encoder;
  @Mock private JwtService jwtService;
  @Mock private AuditoriaService auditoria;

  private AuthService servicio;
  private final Usuario ana = Datos.usuario(10L, "ana");

  @BeforeEach
  void setUp() {
    LimitadorIntentos limitador = new LimitadorIntentos(5, 60, Datos.reloj());
    servicio =
        new AuthService(
            usuarios,
            membresias,
            empresas,
            revocados,
            encoder,
            jwtService,
            limitador,
            new PoliticaContrasena(12),
            auditoria);
  }

  @Test
  void registraUsuarioConHashYAuditoria() {
    when(usuarios.existsByNombreUsuario("ana")).thenReturn(false);
    when(usuarios.existsByCorreoIgnoreCase("ana@atlas.test")).thenReturn(false);
    when(encoder.encode(CLAVE)).thenReturn("$2a$12$hash");
    when(usuarios.save(any(Usuario.class)))
        .thenAnswer(
            inv -> {
              Usuario u = inv.getArgument(0);
              u.setId(10L);
              return u;
            });
    UsuarioResponse respuesta =
        servicio.registrar(new RegistroRequest("ana", "ANA@atlas.test", CLAVE, true));
    assertThat(respuesta.id()).isEqualTo(10L);
    assertThat(respuesta.correo()).isEqualTo("ana@atlas.test");
    verify(auditoria)
        .registrar(
            isNull(),
            eq(10L),
            eq(AccionAuditoria.REGISTRAR_USUARIO),
            eq("usuario"),
            eq(10L),
            anyString());
  }

  @Test
  void rechazaContrasenaDebil() {
    assertThatThrownBy(
            () -> servicio.registrar(new RegistroRequest("ana", "a@x.cl", "corta", true)))
        .isInstanceOf(ValidacionException.class)
        .satisfies(e -> assertThat(((ValidacionException) e).getErrores()).isNotEmpty());
    verify(usuarios, never()).save(any());
  }

  @Test
  void rechazaNombreDeUsuarioDuplicado() {
    when(usuarios.existsByNombreUsuario("ana")).thenReturn(true);
    assertThatThrownBy(() -> servicio.registrar(new RegistroRequest("ana", "a@x.cl", CLAVE, true)))
        .isInstanceOf(ConflictoException.class);
  }

  @Test
  void rechazaCorreoDuplicado() {
    when(usuarios.existsByNombreUsuario("ana")).thenReturn(false);
    when(usuarios.existsByCorreoIgnoreCase("a@x.cl")).thenReturn(true);
    assertThatThrownBy(() -> servicio.registrar(new RegistroRequest("ana", "a@x.cl", CLAVE, true)))
        .isInstanceOf(ConflictoException.class);
  }

  @Test
  void loginExitosoEntregaTokensYEmpresas() {
    when(usuarios.findByNombreUsuario("ana")).thenReturn(Optional.of(ana));
    when(encoder.matches(CLAVE, ana.getPasswordHash())).thenReturn(true);
    TokenEmitido acceso = new TokenEmitido("acc", "j1", Datos.AHORA.plusSeconds(900));
    TokenEmitido refresco = new TokenEmitido("ref", "j2", Datos.AHORA.plusSeconds(9000));
    when(jwtService.emitir(10L, "ana", JwtService.TIPO_ACCESO)).thenReturn(acceso);
    when(jwtService.emitir(10L, "ana", JwtService.TIPO_REFRESCO)).thenReturn(refresco);
    when(jwtService.getDuracionAcceso()).thenReturn(Duration.ofMinutes(15));
    when(membresias.findByUsuarioId(10L)).thenReturn(List.of(new Membresia(1L, 10L, Rol.ADMIN)));
    when(empresas.findById(1L)).thenReturn(Optional.of(Datos.empresa(1L)));

    ResultadoLogin resultado = servicio.login(new LoginRequest("ana", CLAVE), "10.0.0.1");

    assertThat(resultado.sesion().accessToken()).isEqualTo("acc");
    assertThat(resultado.sesion().tipo()).isEqualTo("Bearer");
    assertThat(resultado.sesion().expiraEnSegundos()).isEqualTo(900);
    assertThat(resultado.sesion().empresas()).hasSize(1);
    assertThat(resultado.sesion().empresas().get(0).rol()).isEqualTo(Rol.ADMIN);
    assertThat(resultado.refresco().token()).isEqualTo("ref");
  }

  @Test
  void loginConContrasenaIncorrectaDevuelve401() {
    when(usuarios.findByNombreUsuario("ana")).thenReturn(Optional.of(ana));
    when(encoder.matches("mala", ana.getPasswordHash())).thenReturn(false);
    assertThatThrownBy(() -> servicio.login(new LoginRequest("ana", "mala"), "10.0.0.1"))
        .isInstanceOf(CredencialesInvalidasException.class);
  }

  @Test
  void loginDeUsuarioInexistenteIgualEjecutaBcrypt() {
    when(usuarios.findByNombreUsuario("nadie")).thenReturn(Optional.empty());
    when(encoder.matches(eq("x"), anyString())).thenReturn(false);
    assertThatThrownBy(() -> servicio.login(new LoginRequest("nadie", "x"), "10.0.0.1"))
        .isInstanceOf(CredencialesInvalidasException.class);
    verify(encoder).matches(eq("x"), anyString());
  }

  @Test
  void bloqueaTrasCincoIntentosFallidos() {
    when(usuarios.findByNombreUsuario("ana")).thenReturn(Optional.of(ana));
    when(encoder.matches("mala", ana.getPasswordHash())).thenReturn(false);
    LoginRequest malo = new LoginRequest("ana", "mala");
    for (int i = 0; i < 5; i++) {
      assertThatThrownBy(() -> servicio.login(malo, "10.0.0.9"))
          .isInstanceOf(CredencialesInvalidasException.class);
    }
    assertThatThrownBy(() -> servicio.login(malo, "10.0.0.9"))
        .isInstanceOf(DemasiadosIntentosException.class);
  }

  @Test
  void refrescaConTokenValido() {
    UsuarioAutenticado datos = new UsuarioAutenticado(10L, "ana", "r1", Datos.AHORA);
    when(jwtService.validar("ref", JwtService.TIPO_REFRESCO)).thenReturn(Optional.of(datos));
    when(revocados.existsById("r1")).thenReturn(false);
    when(usuarios.findById(10L)).thenReturn(Optional.of(ana));
    when(jwtService.emitir(10L, "ana", JwtService.TIPO_ACCESO))
        .thenReturn(new TokenEmitido("nuevo", "j3", Datos.AHORA));
    when(jwtService.getDuracionAcceso()).thenReturn(Duration.ofMinutes(15));
    when(membresias.findByUsuarioId(10L)).thenReturn(List.of());

    SesionResponse sesion = servicio.refrescar("ref");

    assertThat(sesion.accessToken()).isEqualTo("nuevo");
  }

  @Test
  void noRefrescaConTokenRevocado() {
    UsuarioAutenticado datos = new UsuarioAutenticado(10L, "ana", "r1", Datos.AHORA);
    when(jwtService.validar("ref", JwtService.TIPO_REFRESCO)).thenReturn(Optional.of(datos));
    when(revocados.existsById("r1")).thenReturn(true);
    assertThatThrownBy(() -> servicio.refrescar("ref"))
        .isInstanceOf(CredencialesInvalidasException.class);
  }

  @Test
  void logoutRevocaAccesoYRefresco() {
    UsuarioAutenticado actual = new UsuarioAutenticado(10L, "ana", "a1", Datos.AHORA);
    when(jwtService.validar("ref", JwtService.TIPO_REFRESCO))
        .thenReturn(Optional.of(new UsuarioAutenticado(10L, "ana", "r1", Datos.AHORA)));
    servicio.logout(actual, "ref");
    verify(revocados, times(2)).save(any(TokenRevocado.class));
  }

  @Test
  void logoutSinCookieRevocaSoloElAcceso() {
    UsuarioAutenticado actual = new UsuarioAutenticado(10L, "ana", "a1", Datos.AHORA);
    when(jwtService.validar(null, JwtService.TIPO_REFRESCO)).thenReturn(Optional.empty());
    servicio.logout(actual, null);
    verify(revocados, times(1)).save(any(TokenRevocado.class));
  }

  @Test
  void perfilDelUsuario() {
    when(usuarios.findById(10L)).thenReturn(Optional.of(ana));
    when(membresias.findByUsuarioId(10L)).thenReturn(List.of());
    PerfilResponse perfil = servicio.perfil(10L);
    assertThat(perfil.usuario().nombreUsuario()).isEqualTo("ana");
    assertThat(perfil.empresas()).isEmpty();
  }

  @Test
  void sanitizaSaltosDeLineaParaElLog() {
    assertThat(AuthService.sanitizar("ana\nADMIN login ok")).isEqualTo("ana_ADMIN login ok");
    assertThat(AuthService.sanitizar(null)).isEmpty();
  }
}
