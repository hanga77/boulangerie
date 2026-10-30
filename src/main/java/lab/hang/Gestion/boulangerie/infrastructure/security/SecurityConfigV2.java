package lab.hang.Gestion.boulangerie.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
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
 * Activé uniquement avec le profil Spring {@code refonte-security}
 * pour ne pas entrer en collision avec SecurityConfig existant.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@Profile("refonte-security")
public class SecurityConfigV2 {

    private final LoginRateLimitFilter loginRateLimitFilter;
    private final Environment environment;

    public SecurityConfigV2(LoginRateLimitFilter loginRateLimitFilter, Environment environment) {
        this.loginRateLimitFilter = loginRateLimitFilter;
        this.environment = environment;
    }

    @Bean
    public SecurityFilterChain filterChainV2(HttpSecurity http,
                                             HandlerMappingIntrospector introspector) throws Exception {
        MvcRequestMatcher.Builder mvc = new MvcRequestMatcher.Builder(introspector);
        boolean isProd = environment.acceptsProfiles(Profiles.of("prod"));

        http
                .addFilterBefore(loginRateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(mvc.pattern("/login")).permitAll();
                    auth.requestMatchers(mvc.pattern("/css/**"), mvc.pattern("/js/**"),
                            mvc.pattern("/images/**"), mvc.pattern("/webfonts/**")).permitAll();
                    if (!isProd) {
                        auth.requestMatchers(mvc.pattern("/swagger-ui/**"),
                                mvc.pattern("/v3/api-docs/**")).permitAll();
                    }
                    auth.requestMatchers(mvc.pattern("/guichet/**")).hasRole("CAISSIER");
                    auth.requestMatchers(mvc.pattern("/matieres-premieres/**"),
                                    mvc.pattern("/api/cycle/**"))
                            .hasAnyRole("ADMIN", "MANAGER", "MAGASINIER", "BOULANGER");
                    auth.requestMatchers(mvc.pattern("/production/**"))
                            .hasAnyRole("ADMIN", "MANAGER", "BOULANGER");
                    auth.anyRequest().authenticated();
                })
                .formLogin(form -> form.loginPage("/login").defaultSuccessUrl("/dashboard", true).permitAll())
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                        .logoutSuccessUrl("/login")
                        .invalidateHttpSession(true)
                        .permitAll())
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp.policyDirectives(
                                "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'"))
                        .referrerPolicy(ref -> ref.policy(
                                ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoderV2() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public RoleHierarchy roleHierarchyV2() {
        return RoleHierarchyImpl.fromHierarchy("""
                ROLE_SUPERADMIN > ROLE_ADMIN
                ROLE_ADMIN > ROLE_MANAGER
                ROLE_MANAGER > ROLE_BOULANGER
                ROLE_MANAGER > ROLE_MAGASINIER
                ROLE_MANAGER > ROLE_CAISSIER
                """);
    }
}
