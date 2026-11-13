package lab.hang.Gestion.boulangerie.application.production;

import lab.hang.Gestion.boulangerie.domain.production.ProductionStatus;
import lab.hang.Gestion.boulangerie.domain.production.ProductionV2;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Port de lecture pour le tableau de bord du cycle journalier.
 */
public interface DailyCycleQueryPort {

    int countCommandes(LocalDate date);

    int countCommandesTraitees(LocalDate date);

    Optional<ProductionV2> findProductionDuJour(LocalDate date);

    int countLivraisonsPourProduction(Long productionId);

    int countVentesGuichet(LocalDate date);

    /** true si une clôture journalière a déjà été enregistrée. */
    boolean isJourCloture(LocalDate date);
}
