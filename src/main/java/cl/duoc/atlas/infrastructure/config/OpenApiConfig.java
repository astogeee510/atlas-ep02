package cl.duoc.atlas.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Metadatos OpenAPI del MVP. El contrato visible en Swagger UI / Redoc es el YAML de
 * /openapi/atlas-openapi.yaml (contract-first).
 */
@Configuration
public class OpenApiConfig {

  /** Nombre del esquema de seguridad JWT. */
  public static final String ESQUEMA_JWT = "bearerAuth";

  @Bean
  public OpenAPI atlasOpenApi() {
    return new OpenAPI()
        .info(
            new Info()
                .title("API Atlas")
                .description(
                    "MVP de Atlas (Caso A - CreaLab SpA): clientes y contratos con login JWT "
                        + "y roles ADMIN / EDITOR")
                .version("1.0.0")
                .contact(new Contact().name("Equipo Atlas - ISY1102"))
                .license(new License().name("Uso académico")))
        .components(
            new Components()
                .addSecuritySchemes(
                    ESQUEMA_JWT,
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
        .addSecurityItem(new SecurityRequirement().addList(ESQUEMA_JWT));
  }
}
