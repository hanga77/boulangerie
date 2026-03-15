package lab.hang.Gestion.boulangerie.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Filtre de limitation du taux de tentatives de connexion.
 * Bloque une adresse IP après MAX_ATTEMPTS tentatives échouées dans WINDOW_SECONDS secondes.
 */
@Component
public class LoginRateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(LoginRateLimitFilter.class);
    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_SECONDS = 300; // 5 minutes

    private final Map<String, AttemptRecord> attempts = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(@org.springframework.lang.NonNull HttpServletRequest request,
                                    @org.springframework.lang.NonNull HttpServletResponse response,
                                    @org.springframework.lang.NonNull FilterChain filterChain) throws ServletException, IOException {

        if ("/login".equals(request.getServletPath()) && "POST".equals(request.getMethod())) {
            String ip = getClientIp(request);
            AttemptRecord record = attempts.computeIfAbsent(ip, k -> new AttemptRecord());

            long now = Instant.now().getEpochSecond();

            // Réinitialiser le compteur si la fenêtre de temps est écoulée
            if (now - record.windowStart > WINDOW_SECONDS) {
                record.count.set(0);
                record.windowStart = now;
            }

            if (record.count.incrementAndGet() > MAX_ATTEMPTS) {
                log.warn("Trop de tentatives de connexion depuis l'IP: {} ({} tentatives)", ip, record.count.get());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                        "Trop de tentatives de connexion. Veuillez réessayer dans 5 minutes.");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static class AttemptRecord {
        AtomicInteger count = new AtomicInteger(0);
        volatile long windowStart = Instant.now().getEpochSecond();
    }
}
