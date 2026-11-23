package lab.hang.Gestion.boulangerie.domain.inventory.events;

import java.time.Instant;

public record StockExitedForProductionEvent(
        Long productionId,
        Long matierePremiereId,
        String matiereNom,
        double quantite,
        Long userId,
        Instant occurredAt
) {
    public static StockExitedForProductionEvent of(Long productionId, Long matiereId,
                                                   String nom, double qte, Long userId) {
        return new StockExitedForProductionEvent(
                productionId, matiereId, nom, qte, userId, Instant.now()
        );
    }
}
