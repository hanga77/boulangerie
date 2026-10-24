package lab.hang.Gestion.boulangerie.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

/**
 * Configuration sécurité — phase 3 P1 : headers, hiérarchie complète, routes cycle V2,
 * Swagger fermé en prod, BCrypt renforcé.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final CustomAuthenticationFailureHandler authenticationFailureHandler;
    private final LoginRateLimitFilter loginRateLimitFilter;
    private final Environment environment;

    public SecurityConfig(CustomAuthenticationFailureHandler authenticationFailureHandler,
                          LoginRateLimitFilter loginRateLimitFilter,
                          Environment environment) {
        this.authenticationFailureHandler = authenticationFailureHandler;
        this.loginRateLimitFilter = loginRateLimitFilter;
        this.environment = environment;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, HandlerMappingIntrospector introspector) throws Exception {
        MvcRequestMatcher.Builder mvc = new MvcRequestMatcher.Builder(introspector);
        boolean isProd = environment.acceptsProfiles(Profiles.of("prod"));

        http
                .addFilterBefore(loginRateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(mvc.pattern("/login")).permitAll();
                    auth.requestMatchers(mvc.pattern("/register")).permitAll();
                    auth.requestMatchers(mvc.pattern("/css/**"), mvc.pattern("/js/**"),
                            mvc.pattern("/images/**"), mvc.pattern("/webfonts/**")).permitAll();

                    if (!isProd) {
                        auth.requestMatchers(mvc.pattern("/swagger-ui/**"),
                                mvc.pattern("/swagger-ui.html"),
                                mvc.pattern("/v3/api-docs/**")).permitAll();
                    }

                    // Cycle V2 (phase 1–3)
                    auth.requestMatchers(mvc.pattern("/cycle/caisse/**"),
                                    mvc.pattern("/cycle/vente/**"))
                            .hasAnyRole("ADMIN", "MANAGER", "CAISSIER");
                    auth.requestMatchers(mvc.pattern("/cycle/feuille/**"))
                            .hasAnyRole("ADMIN", "MANAGER", "MAGASINIER", "BOULANGER");
                    auth.requestMatchers(mvc.pattern("/cycle/**"),
                                    mvc.pattern("/api/cycle/**"))
                            .hasAnyRole("ADMIN", "MANAGER", "BOULANGER", "MAGASINIER", "CAISSIER");

                    auth.requestMatchers(mvc.pattern("/api/credits/**")).hasAnyRole("ADMIN", "MANAGER");
                    auth.requestMatchers(mvc.pattern("/api/fournisseurs/**")).hasAnyRole("ADMIN", "MANAGER");
                    auth.requestMatchers(mvc.pattern("/rapports/**")).hasAnyRole("ADMIN", "MANAGER");
                    auth.requestMatchers(mvc.pattern("/fournisseurs/**")).hasAnyRole("ADMIN", "MANAGER");
                    auth.requestMatchers(mvc.pattern("/production/incidents/**")).hasAnyRole("ADMIN", "MANAGER");
                    auth.requestMatchers(mvc.pattern("/production/**")).hasAnyRole("ADMIN", "MANAGER", "BOULANGER");
                    auth.requestMatchers(mvc.pattern("/commandes/**")).hasAnyRole("ADMIN", "MANAGER", "BOULANGER");
                    auth.requestMatchers(mvc.pattern("/livraisons/**")).hasAnyRole("ADMIN", "MANAGER");
                    auth.requestMatchers(mvc.pattern("/ventes-libres/**")).hasAnyRole("ADMIN", "MANAGER");
                    auth.requestMatchers(mvc.pattern("/guichet/**")).hasRole("CAISSIER");
                    auth.requestMatchers(mvc.pattern("/mon-espace/**")).hasRole("POINT_DE_VENTE");
                    auth.requestMatchers(mvc.pattern("/matieres-premieres/**")).hasAnyRole("ADMIN", "MANAGER", "MAGASINIER");
                    auth.requestMatchers(mvc.pattern("/employes/**")).hasAnyRole("ADMIN", "MANAGER");
                    auth.requestMatchers(mvc.pattern("/bulletins/**")).authenticated();
                    auth.requestMatchers(mvc.pattern("/finances/**")).hasAnyRole("ADMIN", "MANAGER");
                    auth.requestMatchers(mvc.pattern("/compte-bancaire/**")).hasAnyRole("ADMIN", "MANAGER");
                    auth.requestMatchers(mvc.pattern("/comptabilite/**")).hasAnyRole("ADMIN", "MANAGER");
                    auth.requestMatchers(mvc.pattern("/admin/**")).hasAnyRole("SUPERADMIN", "ADMIN");
                    auth.anyRequest().authenticated();
                })
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/dashboard", true)
                        .failureHandler(authenticationFailureHandler)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                        .logoutSuccessUrl("/login")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp.policyDirectives(
                                "default-src 'self'; script-src 'self' 'unsafe-inline'; " +
                                "style-src 'self' 'unsafe-inline'; img-src 'self' data:; font-src 'self' data:"))
                        .referrerPolicy(ref -> ref.policy(
                                ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                        .frameOptions(frame -> frame.sameOrigin())
                )
                .sessionManagement(session -> session
                        .maximumSessions(5)
                        .maxSessionsPreventsLogin(false)
                );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Facteur 12 (phase 3) — plus résistant au brute-force offline
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.fromHierarchy("""
                ROLE_SUPERADMIN > ROLE_ADMIN
                ROLE_ADMIN > ROLE_MANAGER
                ROLE_MANAGER > ROLE_BOULANGER
                ROLE_MANAGER > ROLE_MAGASINIER
                ROLE_MANAGER > ROLE_CAISSIER
                """);
    }
}
