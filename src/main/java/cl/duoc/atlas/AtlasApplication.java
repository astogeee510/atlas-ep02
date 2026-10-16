package cl.duoc.atlas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

/**
 * Punto de entrada del MVP de Atlas (Caso A - CreaLab SpA).
 *
 * <p>Se excluye el usuario en memoria que Spring Security crea por defecto: los usuarios se
 * autentican contra la tabla de usuarios con bcrypt y reciben un JWT.
 */
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class AtlasApplication {

  public static void main(String[] args) {
    SpringApplication.run(AtlasApplication.class, args);
  }
}
