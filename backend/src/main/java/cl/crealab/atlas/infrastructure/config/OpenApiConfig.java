package cl.crealab.atlas.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Metadatos OpenAPI (Swagger UI en /swagger-ui.html, ERS 3.1.2). */
@Configuration
public class OpenApiConfig {

  private static final String ESQUEMA = "bearerAuth";

  @Bean
  public OpenAPI atlasOpenApi() {
    return new OpenAPI()
        .info(
            new Info()
                .title("Atlas API")
                .description("Gestion de clientes, contratos y documentos para pymes (CreaLab SpA)")
                .version("1.0.0")
                .contact(new Contact().name("CreaLab SpA - Departamento de desarrollo"))
                .license(new License().name("Uso academico")))
        .components(
            new Components()
                .addSecuritySchemes(
                    ESQUEMA,
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
        .addSecurityItem(new SecurityRequirement().addList(ESQUEMA));
  }
}
