package lab.hang.Gestion.boulangerie.infrastructure.license;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;

@Service
@Profile("refonte-license")
public class LicenseServiceV2 {

    private static final int DEMO_DAYS = 90;
    private static final String PREFIX = "GESTIBOUL-";

    private final LicenseConfigPort repo;

    @Value("${app.license.salt:}")
    private String salt;

    public LicenseServiceV2(LicenseConfigPort repo) {
        this.repo = repo;
    }

    public enum Status { DEMO_ACTIVE, DEMO_EXPIRED, FULL }

    public Status getStatus() {
        var lic = repo.getOrInit();
        if ("FULL".equals(lic.type())) return Status.FULL;
        long days = ChronoUnit.DAYS.between(lic.installedAt(), LocalDate.now());
        return days <= DEMO_DAYS ? Status.DEMO_ACTIVE : Status.DEMO_EXPIRED;
    }

    public String getMachineId() {
        try {
            StringBuilder raw = new StringBuilder();
            NetworkInterface.networkInterfaces()
                    .filter(n -> {
                        try { return !n.isLoopback() && n.isUp() && n.getHardwareAddress() != null; }
                        catch (Exception e) { return false; }
                    })
                    .forEach(n -> {
                        try {
                            for (byte b : n.getHardwareAddress()) raw.append(String.format("%02X", b));
                        } catch (Exception ignored) {}
                    });
            raw.append(System.getProperty("os.name", ""));
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(raw.toString().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash).substring(0, 12).toUpperCase();
        } catch (Exception e) {
            return "UNKNOWN000000";
        }
    }

    @Transactional
    public boolean activate(String key) {
        requireSalt();
        if (key == null || !key.startsWith(PREFIX)) return false;
        String[] parts = key.substring(PREFIX.length()).split("-");
        if (parts.length != 2) return false;
        if (!hmac(parts[0]).equalsIgnoreCase(parts[1])) return false;
        if (!getMachineId().equalsIgnoreCase(parts[0])) return false;
        repo.activateFull();
        return true;
    }

    private String hmac(String clientId) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(salt.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(clientId.toUpperCase().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash).substring(0, 12).toUpperCase();
        } catch (Exception e) {
            throw new IllegalStateException("Erreur calcul licence", e);
        }
    }

    private void requireSalt() {
        if (salt == null || salt.isBlank() || "gestiboul-secret-2024".equals(salt)) {
            throw new IllegalStateException("app.license.salt doit être une valeur forte");
        }
    }

    public interface LicenseConfigPort {
        record LicenseView(String type, LocalDate installedAt) {}
        LicenseView getOrInit();
        void activateFull();
    }
}
