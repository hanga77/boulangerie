package lab.hang.Gestion.boulangerie.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Phase 10 — sonde légère pour reverse-proxy / monitoring (sans Actuator).
 */
@RestController
@RequestMapping("/api/public")
public class PublicHealthController {

    @GetMapping(value = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> health() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("status", "UP");
        m.put("service", "gestiboul");
        m.put("timestamp", Instant.now().toString());
        return m;
    }
}
