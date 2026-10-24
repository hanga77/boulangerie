package lab.hang.Gestion.boulangerie.application.inventory;

import lab.hang.Gestion.boulangerie.domain.inventory.events.StockExitedForProductionEvent;
import lab.hang.Gestion.boulangerie.domain.production.ProductionLigneMatiere;
import lab.hang.Gestion.boulangerie.domain.production.ProductionV2;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case critique : le magasinier confirme la sortie réelle liée à une production.
 */
@Service
public class ConfirmStockExitForProductionUseCase {

    private final InventoryPort inventoryPort;
    private final ProductionPort productionPort;
    private final ApplicationEventPublisher events;

    public ConfirmStockExitForProductionUseCase(InventoryPort inventoryPort,
                                                ProductionPort productionPort,
                                                ApplicationEventPublisher events) {
        this.inventoryPort = inventoryPort;
        this.productionPort = productionPort;
        this.events = events;
    }

    public record Command(Long productionId, Long matierePremiereId,
                          double quantite, Long userId) {}

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void execute(Command cmd) {
        if (cmd.quantite() <= 0) {
            throw new IllegalArgumentException("La quantité doit être positive");
        }
        if (cmd.productionId() == null) {
            throw new IllegalArgumentException("La production de référence est obligatoire");
        }

        ProductionV2 production = productionPort.getById(cmd.productionId());

        double stock = inventoryPort.stockActuel(cmd.matierePremiereId());
        if (stock < cmd.quantite()) {
            throw new IllegalStateException(
                    "Stock insuffisant pour " + inventoryPort.nomMatiere(cmd.matierePremiereId())
                            + " (dispo: " + stock + ", demandé: " + cmd.quantite() + ")");
        }

        inventoryPort.diminuerStockAvecVerrou(cmd.matierePremiereId(), cmd.quantite());
        inventoryPort.enregistrerMouvementSortieProduction(
                cmd.matierePremiereId(), cmd.quantite(), cmd.productionId(), cmd.userId());

        ProductionLigneMatiere ligne = production.ligneMatiere(cmd.matierePremiereId())
                .orElseGet(() -> {
                    production.ajouterOuCumulerMatiere(cmd.matierePremiereId(), 0);
                    return production.ligneMatiere(cmd.matierePremiereId()).orElseThrow();
                });
        double deja = ligne.getQuantiteMagasinier() != null ? ligne.getQuantiteMagasinier() : 0;
        ligne.confirmerMagasinier(deja + cmd.quantite());
        productionPort.save(production);

        events.publishEvent(StockExitedForProductionEvent.of(
                cmd.productionId(),
                cmd.matierePremiereId(),
                inventoryPort.nomMatiere(cmd.matierePremiereId()),
                cmd.quantite(),
                cmd.userId()
        ));
    }
}
