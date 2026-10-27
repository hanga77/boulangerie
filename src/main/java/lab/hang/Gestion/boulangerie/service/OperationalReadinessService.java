package lab.hang.Gestion.boulangerie.service;

import lab.hang.Gestion.boulangerie.application.production.ProductionPersistencePort;
import lab.hang.Gestion.boulangerie.repository.CompteBancaireRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Contrôles légers pour réduire les fragilités opérationnelles
 * (Compte Principal, double flux V1/V2 le même jour).
 */
@Service
public class OperationalReadinessService {

    public record Alert(String level, String message, String actionUrl, String actionLabel) {}

    private final CompteBancaireRepository compteBancaireRepository;
    private final ProductionPersistencePort productionPersistencePort;
    private final ProductionService productionService;

    public OperationalReadinessService(CompteBancaireRepository compteBancaireRepository,
                                       ProductionPersistencePort productionPersistencePort,
                                       ProductionService productionService) {
        this.compteBancaireRepository = compteBancaireRepository;
        this.productionPersistencePort = productionPersistencePort;
        this.productionService = productionService;
    }

    public boolean hasComptePrincipal() {
        return compteBancaireRepository.findByNom("Compte Principal").isPresent();
    }

    public List<Alert> alertsForToday() {
        LocalDate today = LocalDate.now();
        List<Alert> alerts = new ArrayList<>();

        if (!hasComptePrincipal()) {
            alerts.add(new Alert(
                    "danger",
                    "Compte Principal introuvable. Les livraisons V2, dépenses et recettes peuvent échouer.",
                    "/compte-bancaire/creer",
                    "Créer un compte"
            ));
        }

        boolean v2 = false;
        try {
            v2 = productionPersistencePort.existsForDate(today)
                    || !productionPersistencePort.findByDate(today).isEmpty();
        } catch (Exception ignored) {
            // tables V2 absentes tant que migrations non appliquées
        }

        boolean v1 = false;
        try {
            v1 = productionService.getProductionsByDate(today) != null
                    && !productionService.getProductionsByDate(today).isEmpty();
        } catch (Exception ignored) {
        }

        if (v1 && v2) {
            alerts.add(new Alert(
                    "warning",
                    "Attention : production V1 et cycle V2 détectés aujourd'hui. Utilisez un seul flux pour éviter les doubles stocks / double CA.",
                    "/cycle",
                    "Aller au cycle V2"
            ));
        } else if (v1 && !v2) {
            alerts.add(new Alert(
                    "info",
                    "Production historique (V1) en cours. Le flux recommandé est le Cycle du jour (V2).",
                    "/cycle",
                    "Ouvrir le cycle"
            ));
        }

        if (!v1 && !v2) {
            alerts.add(new Alert(
                    "info",
                    "Aucune production lancée aujourd'hui. Démarrez le Cycle du jour pour le parcours recommandé.",
                    "/cycle",
                    "Démarrer le cycle"
            ));
        }

        return alerts;
    }
}
