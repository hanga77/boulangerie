package lab.hang.Gestion.boulangerie.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Phase 10 — point d'entrée stub pour intégrations externes (WhatsApp, ERP…).
 * Journalise le payload ; pas d'effet métier tant que le pilote n'est pas validé.
 */
@RestController
@RequestMapping("/api/integrations")
@PreAuthorize("hasRole('ADMIN')")
public class IntegrationWebhookController {

    private static final Logger log = LoggerFactory.getLogger(IntegrationWebhookController.class);

    @PostMapping(value = "/webhook", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> webhook(@RequestBody(required = false) Map<String, Object> body) {
        log.info("Webhook intégration reçu (stub): keys={}",
                body != null ? body.keySet() : "null");
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("accepted", true);
        resp.put("mode", "stub");
        resp.put("receivedAt", Instant.now().toString());
        resp.put("message", "Payload enregistré en log uniquement. Brancher le provider réel en phase suivante.");
        return resp;
    }
}
