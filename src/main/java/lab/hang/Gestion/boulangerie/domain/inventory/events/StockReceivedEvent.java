package lab.hang.Gestion.boulangerie.domain.inventory.events;

import java.time.Instant;

/**
 * Émis après une entrée de stock validée.
 * FinanceService écoute cet événement pour créer la transaction ACHAT / ACHAT_CREDIT.
 */
public record StockReceivedEvent(
        Long matierePremiereId,
        String matiereNom,
        double quantiteNette,
        double prixUnitaire,
        double coutTotal,
        Long userId,
        String username,
        Instant occurredAt
) {
    public static StockReceivedEvent of(Long matiereId, String nom, double qte,
                                        double prixUnitaire, Long userId, String username) {
        return new StockReceivedEvent(
                matiereId, nom, qte, prixUnitaire,
                qte * prixUnitaire, userId, username, Instant.now()
        );
    }
}
