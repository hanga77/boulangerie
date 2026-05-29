package lab.hang.Gestion.boulangerie.service;

import lab.hang.Gestion.boulangerie.model.LicenseConfig;
import lab.hang.Gestion.boulangerie.repository.LicenseConfigRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class LicenseService {

    private static final int DEMO_DAYS = 90;
    private static final String PREFIX = "GESTIBOUL-";

    private final LicenseConfigRepository repo;

    @Value("${app.license.salt:gestiboul-secret-2024}")
    private String salt;

    public LicenseService(LicenseConfigRepository repo) {
        this.repo = repo;
    }

    public enum Status { DEMO_ACTIVE, DEMO_EXPIRED, FULL }

    /** Retourne le statut courant de la licence. */
    public Status getStatus() {
        LicenseConfig lic = getOrInit();
        if ("FULL".equals(lic.getType())) return Status.FULL;
        long days = ChronoUnit.DAYS.between(lic.getInstalledAt(), LocalDate.now());
        return days <= DEMO_DAYS ? Status.DEMO_ACTIVE : Status.DEMO_EXPIRED;
    }

    /** Jours restants en démo (0 si expiré ou FULL). */
    public long demoJoursRestants() {
        LicenseConfig lic = getOrInit();
        if ("FULL".equals(lic.getType())) return 0;
        long elapsed = ChronoUnit.DAYS.between(lic.getInstalledAt(), LocalDate.now());
        return Math.max(0, DEMO_DAYS - elapsed);
    }

    public LocalDate getInstalledAt() {
        return getOrInit().getInstalledAt();
    }

    /** Valide et active la clé. Retourne true si succès. */
    @Transactional
    public boolean activate(String key) {
        if (key == null || !key.startsWith(PREFIX)) return false;
        String rest = key.substring(PREFIX.length()); // ex: "A3F9B2-1C8E4D"
        String[] parts = rest.split("-");
        if (parts.length != 2) return false;
        String clientId = parts[0];
        String checksum = parts[1];
        String expected = computeChecksum(clientId);
        if (!expected.equalsIgnoreCase(checksum)) return false;

        LicenseConfig lic = getOrInit();
        lic.setType("FULL");
        lic.setActivatedAt(LocalDate.now());
        repo.save(lic);
        return true;
    }

    /** Génère une clé pour un clientId donné (usage interne / script). */
    public String generateKey(String clientId) {
        return PREFIX + clientId.toUpperCase() + "-" + computeChecksum(clientId);
    }

    // ── Interne ──────────────────────────────────────────────────────────────

    private String computeChecksum(String clientId) {
        try {
            String input = salt + clientId.toUpperCase();
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02X", b));
            return sb.substring(0, 6);
        } catch (Exception e) {
            throw new RuntimeException("Erreur calcul checksum", e);
        }
    }

    private LicenseConfig getOrInit() {
        return repo.findById(1L).orElseGet(() -> {
            LicenseConfig lic = new LicenseConfig();
            lic.setInstalledAt(LocalDate.now());
            return repo.save(lic);
        });
    }
}
