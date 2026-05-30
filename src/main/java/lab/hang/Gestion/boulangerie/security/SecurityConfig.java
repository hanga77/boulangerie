package lab.hang.Gestion.boulangerie.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final CustomAuthenticationFailureHandler authenticationFailureHandler;
    private final LoginRateLimitFilter loginRateLimitFilter;

    public SecurityConfig(CustomAuthenticationFailureHandler authenticationFailureHandler,
                          LoginRateLimitFilter loginRateLimitFilter) {
        this.authenticationFailureHandler = authenticationFailureHandler;
        this.loginRateLimitFilter = loginRateLimitFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, HandlerMappingIntrospector introspector) throws Exception {
        MvcRequestMatcher.Builder mvcMatcherBuilder = new MvcRequestMatcher.Builder(introspector);

        http
                .addFilterBefore(loginRateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(mvcMatcherBuilder.pattern("/login")).permitAll()
                        .requestMatchers(mvcMatcherBuilder.pattern("/register")).permitAll()
                        .requestMatchers(mvcMatcherBuilder.pattern("/css/**")).permitAll()
                        .requestMatchers(mvcMatcherBuilder.pattern("/js/**")).permitAll()
                        .requestMatchers(mvcMatcherBuilder.pattern("/images/**")).permitAll()
                        .requestMatchers(mvcMatcherBuilder.pattern("/swagger-ui/**")).permitAll()
                        .requestMatchers(mvcMatcherBuilder.pattern("/swagger-ui.html")).permitAll()
                        .requestMatchers(mvcMatcherBuilder.pattern("/v3/api-docs/**")).permitAll()
                        .requestMatchers(mvcMatcherBuilder.pattern("/api/credits/**")).hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(mvcMatcherBuilder.pattern("/api/fournisseurs/**")).hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(mvcMatcherBuilder.pattern("/rapports/**")).hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(mvcMatcherBuilder.pattern("/fournisseurs/**")).hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(mvcMatcherBuilder.pattern("/production/incidents/**")).hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(mvcMatcherBuilder.pattern("/production/**")).hasAnyRole("ADMIN", "MANAGER", "BOULANGER")
                        .requestMatchers(mvcMatcherBuilder.pattern("/commandes/**")).hasAnyRole("ADMIN", "MANAGER", "BOULANGER")
                        .requestMatchers(mvcMatcherBuilder.pattern("/livraisons/**")).hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(mvcMatcherBuilder.pattern("/ventes-libres/**")).hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(mvcMatcherBuilder.pattern("/guichet/**")).hasRole("CAISSIER")
                        .requestMatchers(mvcMatcherBuilder.pattern("/matieres-premieres/**")).hasAnyRole("ADMIN", "MANAGER", "MAGASINIER")
                        .requestMatchers(mvcMatcherBuilder.pattern("/employes/**")).hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(mvcMatcherBuilder.pattern("/bulletins/**")).authenticated()
                        .requestMatchers(mvcMatcherBuilder.pattern("/finances/**")).hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(mvcMatcherBuilder.pattern("/compte-bancaire/**")).hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(mvcMatcherBuilder.pattern("/comptabilite/**")).hasAnyRole("ADMIN", "MANAGER")
                        .anyRequest().authenticated()
                )
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
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.fromHierarchy(
            "ROLE_SUPERADMIN > ROLE_ADMIN\n" +
            "ROLE_ADMIN > ROLE_MANAGER\n" +
            "ROLE_MANAGER > ROLE_BOULANGER"
        );
    }
}