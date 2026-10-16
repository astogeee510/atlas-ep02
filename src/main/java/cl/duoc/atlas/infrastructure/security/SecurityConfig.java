package cl.duoc.atlas.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuracion de Spring Security (NFR-SEG-4, NFR-SEG-9).
 *
 * <ul>
 *   <li>API sin estado: cada peticion trae su JWT en el header Authorization.
 *   <li>CSRF deshabilitado de forma justificada: el token no viaja en cookies, por lo que el
 *       navegador no lo envia automaticamente desde otro sitio.
 *   <li>Lectura (GET): ADMIN y EDITOR. Escritura (POST, PUT, DELETE): solo ADMIN (RB-4, RB-5).
 *   <li>Contrasenas con bcrypt costo 12 (NFR-SEG-2).
 * </ul>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private static final int COSTO_BCRYPT = 12;
  private static final String API = "/api/v1/**";
  private static final String ADMIN = "ADMIN";

  @Bean
  public SecurityFilterChain filterChain(
      HttpSecurity http, JwtService jwtService, ObjectMapper objectMapper) throws Exception {
    RespuestaErrorSeguridad errores = new RespuestaErrorSeguridad(objectMapper);
    http.csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(HttpMethod.POST, "/api/v1/auth/login")
                    .permitAll()
                    .requestMatchers(
                        "/",
                        "/index.html",
                        "/redoc.html",
                        "/favicon.ico",
                        "/css/**",
                        "/openapi/**",
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/error")
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/v1/auth/logout")
                    .authenticated()
                    .requestMatchers(HttpMethod.GET, API)
                    .hasAnyRole(ADMIN, "EDITOR")
                    .requestMatchers(API)
                    .hasRole(ADMIN)
                    .anyRequest()
                    .denyAll())
        .exceptionHandling(e -> e.authenticationEntryPoint(errores).accessDeniedHandler(errores))
        .httpBasic(AbstractHttpConfigurer::disable)
        .formLogin(AbstractHttpConfigurer::disable)
        .logout(AbstractHttpConfigurer::disable)
        .addFilterBefore(new JwtAuthFilter(jwtService), UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(COSTO_BCRYPT);
  }
}
