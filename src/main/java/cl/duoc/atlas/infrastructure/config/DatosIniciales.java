package cl.duoc.atlas.infrastructure.config;

import cl.duoc.atlas.domain.gateway.ClienteGateway;
import cl.duoc.atlas.domain.gateway.ContratoGateway;
import cl.duoc.atlas.domain.gateway.UsuarioGateway;
import cl.duoc.atlas.domain.model.Cliente;
import cl.duoc.atlas.domain.model.Contrato;
import cl.duoc.atlas.domain.model.Rol;
import cl.duoc.atlas.domain.model.Usuario;
import java.time.Clock;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Carga inicial al arrancar.
 *
 * <ul>
 *   <li>Usuarios "admin" (ADMIN) y "editor" (EDITOR) con la contrasena de las variables
 *       ATLAS_ADMIN_PASSWORD y ATLAS_EDITOR_PASSWORD, guardada como hash bcrypt.
 *   <li>Opcional (atlas.datos-demo=true): tres clientes de ejemplo con un contrato en cada estado.
 * </ul>
 */
@Component
public class DatosIniciales implements ApplicationRunner {

  private static final Logger LOG = LoggerFactory.getLogger(DatosIniciales.class);

  private final UsuarioGateway usuarioGateway;
  private final ClienteGateway clienteGateway;
  private final ContratoGateway contratoGateway;
  private final PasswordEncoder passwordEncoder;
  private final Clock clock;
  private final String passwordAdmin;
  private final String passwordEditor;
  private final boolean datosDemo;

  public DatosIniciales(
      UsuarioGateway usuarioGateway,
      ClienteGateway clienteGateway,
      ContratoGateway contratoGateway,
      PasswordEncoder passwordEncoder,
      Clock clock,
      @Value("${atlas.usuarios.admin-password}") String passwordAdmin,
      @Value("${atlas.usuarios.editor-password}") String passwordEditor,
      @Value("${atlas.datos-demo:false}") boolean datosDemo) {
    this.usuarioGateway = usuarioGateway;
    this.clienteGateway = clienteGateway;
    this.contratoGateway = contratoGateway;
    this.passwordEncoder = passwordEncoder;
    this.clock = clock;
    this.passwordAdmin = passwordAdmin;
    this.passwordEditor = passwordEditor;
    this.datosDemo = datosDemo;
  }

  @Override
  public void run(ApplicationArguments args) {
    crearUsuarioSiNoExiste("admin", passwordAdmin, Rol.ADMIN);
    crearUsuarioSiNoExiste("editor", passwordEditor, Rol.EDITOR);
    if (datosDemo && clienteGateway.findByRut("76123456-0").isEmpty()) {
      cargarDemo();
    }
  }

  private void crearUsuarioSiNoExiste(String nombre, String password, Rol rol) {
    if (usuarioGateway.findByNombreUsuario(nombre).isEmpty()) {
      usuarioGateway.save(new Usuario(null, nombre, passwordEncoder.encode(password), rol, true));
      LOG.info("Usuario inicial creado: {} ({})", nombre, rol);
    }
  }

  private void cargarDemo() {
    LocalDate hoy = LocalDate.now(clock);
    Cliente ferreteria =
        guardar("76123456-0", "Ferretería Los Andes", "Av. Matta 500, Santiago", "56911111111");
    Cliente panaderia =
        guardar("77654321-7", "Panadería La Espiga", "Calle Larga 45, Maipú", "56922222222");
    guardar("78111222-4", "Constructora Sur", "Ruta 5 Sur km 20, Rancagua", "56933333333");
    contrato(ferreteria, "Suministro anual", hoy.minusDays(60), hoy.plusDays(300));
    contrato(ferreteria, "Mantención de bodega", hoy.minusDays(300), hoy.plusDays(15));
    contrato(panaderia, "Arriendo de horno", hoy.minusDays(400), hoy.minusDays(10));
    LOG.info("Datos de demostración cargados (3 clientes, 3 contratos)");
  }

  private Cliente guardar(String rut, String nombre, String direccion, String telefono) {
    return clienteGateway.save(
        new Cliente(
            null,
            rut,
            nombre,
            "Cliente de demostración",
            direccion,
            "contacto@ejemplo.cl",
            telefono,
            true));
  }

  private void contrato(Cliente cliente, String titulo, LocalDate inicio, LocalDate termino) {
    contratoGateway.save(
        new Contrato(null, cliente.getId(), titulo, "Contrato de demostración", inicio, termino));
  }
}
