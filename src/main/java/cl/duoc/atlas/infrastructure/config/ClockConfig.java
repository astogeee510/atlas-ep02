package cl.duoc.atlas.infrastructure.config;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Reloj del sistema en la zona horaria de Chile. Se inyecta para que el calculo del estado de los
 * contratos y del bloqueo de login sea determinista en las pruebas.
 */
@Configuration
public class ClockConfig {

  @Bean
  public Clock clock() {
    return Clock.system(ZoneId.of("America/Santiago"));
  }
}
