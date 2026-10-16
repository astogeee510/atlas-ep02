package cl.crealab.atlas.infrastructure.config;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Reloj inyectable: permite probar fechas de contratos y expiracion de tokens. */
@Configuration
public class ClockConfig {

  @Bean
  public Clock clock() {
    return Clock.system(ZoneId.of("America/Santiago"));
  }
}
