package cl.crealab.atlas.application;

import cl.crealab.atlas.domain.service.PoliticaContrasena;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Expone las reglas de dominio configurables como beans. */
@Configuration
public class DominioConfig {

  @Bean
  public PoliticaContrasena politicaContrasena(
      @Value("${atlas.password.largo-minimo}") int largoMinimo) {
    return new PoliticaContrasena(largoMinimo);
  }
}
