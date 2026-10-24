package lab.hang.Gestion.boulangerie.infrastructure.security;

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
 * Rate limit login (fenêtre glissante).
 */
@Component
public class LoginRateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(LoginRateLimitFilter.class);
    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_SECONDS = 300;

    private final Map<String, AttemptRecord> attempts = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if ("/login".equals(request.getServletPath()) && "POST".equals(request.getMethod())) {
            String ip = clientIp(request);
            AttemptRecord record = attempts.computeIfAbsent(ip, k -> new AttemptRecord());
            long now = Instant.now().getEpochSecond();

            if (now - record.windowStart > WINDOW_SECONDS) {
                record.count.set(0);
                record.windowStart = now;
            }

            if (record.count.incrementAndGet() > MAX_ATTEMPTS) {
                log.warn("Trop de tentatives de connexion depuis IP {} ({} essais)", ip, record.count.get());
                response.sendError(HttpServletResponse.SC_TOO_MANY_REQUESTS,
                        "Trop de tentatives. Réessayez dans 5 minutes.");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String clientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static class AttemptRecord {
        final AtomicInteger count = new AtomicInteger(0);
        volatile long windowStart = Instant.now().getEpochSecond();
    }
}
