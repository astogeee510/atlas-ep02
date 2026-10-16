package cl.duoc.atlas.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import io.swagger.v3.oas.models.OpenAPI;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;

class ConfigTest {

  @Test
  void openApiDeclaraMetadatosYEsquemaJwt() {
    OpenAPI api = new OpenApiConfig().atlasOpenApi();

    assertThat(api.getInfo().getTitle()).isEqualTo("API Atlas");
    assertThat(api.getInfo().getVersion()).isEqualTo("1.0.0");
    assertThat(api.getComponents().getSecuritySchemes()).containsKey(OpenApiConfig.ESQUEMA_JWT);
    assertThat(api.getComponents().getSecuritySchemes().get(OpenApiConfig.ESQUEMA_JWT).getScheme())
        .isEqualTo("bearer");
  }

  @Test
  void elRelojUsaLaZonaDeChile() {
    assertThat(new ClockConfig().clock().getZone()).isEqualTo(ZoneId.of("America/Santiago"));
  }
}
