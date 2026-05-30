package lab.hang.Gestion.boulangerie.service;

import lab.hang.Gestion.boulangerie.model.LicenseConfig;
import lab.hang.Gestion.boulangerie.repository.LicenseConfigRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.NetworkInterface;
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

    /** Empreinte matérielle de ce poste (8 hex chars, basée sur l'adresse MAC). */
    public String getMachineId() {
        try {
            NetworkInterface ni = NetworkInterface.networkInterfaces()
                .filter(n -> {
                    try { return !n.isLoopback() && n.isUp() && n.getHardwareAddress() != null; }
                    catch (Exception e) { return false; }
                })
                .findFirst().orElse(null);
            if (ni == null) return "UNKNOWN";
            byte[] mac = ni.getHardwareAddress();
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(mac);
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02X", b));
            return sb.substring(0, 8);
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }

    /** Valide et active la clé. Retourne true si succès. */
    @Transactional
    public boolean activate(String key) {
        if (key == null || !key.startsWith(PREFIX)) return false;
        String rest = key.substring(PREFIX.length());
        String[] parts = rest.split("-");
        if (parts.length != 2) return false;
        String clientId = parts[0];
        String checksum = parts[1];
        if (!computeChecksum(clientId).equalsIgnoreCase(checksum)) return false;
        if (!getMachineId().equalsIgnoreCase(clientId)) return false;

        LicenseConfig lic = getOrInit();
        lic.setType("FULL");
        lic.setActivatedAt(LocalDate.now());
        repo.save(lic);
        return true;
    }

    /** Génère une clé pour un machineId donné (usage outil KeyGenerator). */
    public String generateKey(String machineId) {
        return PREFIX + machineId.toUpperCase() + "-" + computeChecksum(machineId);
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
