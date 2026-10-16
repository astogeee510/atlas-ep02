package cl.crealab.atlas.infrastructure.config;

import cl.crealab.atlas.infrastructure.persistence.TokenRevocadoRepository;
import cl.crealab.atlas.infrastructure.security.JwtAuthFilter;
import cl.crealab.atlas.infrastructure.security.JwtService;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Configuracion de Spring Security.
 *
 * <ul>
 *   <li>API sin estado: la sesion viaja en un JWT de acceso por header Authorization (NFR-SEG-9).
 *   <li>CSRF deshabilitado de forma justificada: el navegador nunca envia el token de acceso de
 *       forma automatica (no es cookie) y la cookie de refresco es HttpOnly + SameSite=Strict, con
 *       path restringido a /api/auth (NFR-SEG-5).
 *   <li>Contrasenas con bcrypt costo 12 (NFR-SEG-2).
 *   <li>El control por rol y por empresa se aplica en la capa de aplicacion (NFR-SEG-4).
 * </ul>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private static final int COSTO_BCRYPT = 12;

  @Bean
  public SecurityFilterChain filterChain(
      HttpSecurity http, JwtService jwtService, TokenRevocadoRepository revocados)
      throws Exception {
    http.csrf(csrf -> csrf.disable())
        .cors(Customizer.withDefaults())
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(
                        "/api/auth/login",
                        "/api/auth/register",
                        "/api/auth/refresh",
                        "/api/compartido/**",
                        "/actuator/health",
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/error")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .exceptionHandling(
            e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
        .httpBasic(basic -> basic.disable())
        .formLogin(form -> form.disable())
        .addFilterBefore(
            new JwtAuthFilter(jwtService, revocados), UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(COSTO_BCRYPT);
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource(
      @Value("${atlas.cors.origenes}") String origenes) {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(Arrays.stream(origenes.split(",")).map(String::trim).toList());
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
    config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
    config.setAllowCredentials(true);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/**", config);
    return source;
  }
}
