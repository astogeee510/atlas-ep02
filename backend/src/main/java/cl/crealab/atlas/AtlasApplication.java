package cl.crealab.atlas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

/**
 * Punto de arranque de la API de Atlas. Se excluye el usuario en memoria por defecto de Spring
 * Security para que no se genere ni se escriba una contrasena en el log (NFR-SEG-6).
 */
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class AtlasApplication {

  public static void main(String[] args) {
    SpringApplication.run(AtlasApplication.class, args);
  }
}
