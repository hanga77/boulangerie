package lab.hang.Gestion.boulangerie.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

/**
 * Phase 3 P1 — en profil {@code prod}, refuse de démarrer si le salt de licence
 * est absent ou égal à la valeur démo trop faible.
 */
@Component
public class ProdLicenseGuard {

    private static final Logger log = LoggerFactory.getLogger(ProdLicenseGuard.class);
    private static final String WEAK_DEFAULT = "gestiboul-secret-2024";

    private final Environment environment;

    @Value("${app.license.salt:}")
    private String licenseSalt;

    public ProdLicenseGuard(Environment environment) {
        this.environment = environment;
    }

    @PostConstruct
    public void validate() {
        boolean isProd = environment.acceptsProfiles(Profiles.of("prod"));
        if (!isProd) {
            if (licenseSalt == null || licenseSalt.isBlank()) {
                log.warn("app.license.salt non défini — acceptable hors prod uniquement");
            }
            return;
        }

        if (licenseSalt == null || licenseSalt.isBlank() || WEAK_DEFAULT.equals(licenseSalt)) {
            throw new IllegalStateException(
                    "PROFIL PROD : définissez une valeur forte pour app.license.salt " +
                    "(ex. export LICENSE_SALT=$(openssl rand -hex 32)). " +
                    "Valeur absente ou trop faible interdite.");
        }

        if (licenseSalt.length() < 16) {
            throw new IllegalStateException(
                    "PROFIL PROD : app.license.salt trop court (min. 16 caractères).");
        }

        log.info("ProdLicenseGuard : salt de licence OK (longueur {})", licenseSalt.length());
    }
}
