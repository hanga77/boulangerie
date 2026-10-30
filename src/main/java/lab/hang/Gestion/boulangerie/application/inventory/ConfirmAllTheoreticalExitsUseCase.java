package lab.hang.Gestion.boulangerie.application.inventory;

import lab.hang.Gestion.boulangerie.domain.inventory.events.StockExitedForProductionEvent;
import lab.hang.Gestion.boulangerie.domain.production.ProductionLigneMatiere;
import lab.hang.Gestion.boulangerie.domain.production.ProductionV2;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Le magasinier confirme la sortie pour la production.
 * Par défaut il sort le théorique (commandes + farinage) déjà calculé au lancement.
 * Il peut ajuster ligne par ligne si besoin (casse, stock réel différent).
 */
@Service
public class ConfirmAllTheoreticalExitsUseCase {

    private final InventoryPort inventoryPort;
    private final ProductionPort productionPort;
    private final ApplicationEventPublisher events;

    public ConfirmAllTheoreticalExitsUseCase(InventoryPort inventoryPort,
                                             ProductionPort productionPort,
                                             ApplicationEventPublisher events) {
        this.inventoryPort = inventoryPort;
        this.productionPort = productionPort;
        this.events = events;
    }

    /**
     * @param ajustements matiereId → quantité réellement sortie ;
     *                    si absent, on prend le théorique restant à sortir.
     */
    public record Command(
            Long productionId,
            Long userId,
            Map<Long, Double> ajustements
    ) {
        public Command(Long productionId, Long userId) {
            this(productionId, userId, Map.of());
        }
    }

    public record LigneResultat(
            Long matiereId,
            String nom,
            double quantiteSortie,
            boolean farinageInclus
    ) {}

    public record Result(Long productionId, List<LigneResultat> sorties) {}

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Result execute(Command cmd) {
        ProductionV2 production = productionPort.getById(cmd.productionId());
        Map<Long, Double> ajustements =
                cmd.ajustements() != null ? cmd.ajustements() : Map.of();

        List<LigneResultat> sorties = new ArrayList<>();

        for (ProductionLigneMatiere ligne : production.getLignesMatieres()) {
            Long matId = ligne.getMatierePremiereId();
            double deja = ligne.getQuantiteMagasinier() != null ? ligne.getQuantiteMagasinier() : 0;
            double theoriqueRestant = Math.max(0, ligne.getQuantiteTheorique() - deja);

            if (theoriqueRestant <= 0 && !ajustements.containsKey(matId)) {
                continue; // déjà entièrement sorti
            }

            double aSortir = ajustements.containsKey(matId)
                    ? ajustements.get(matId)
                    : theoriqueRestant;

            if (aSortir <= 0) {
                continue;
            }

            double stock = inventoryPort.stockActuel(matId);
            if (stock < aSortir) {
                throw new IllegalStateException(
                        "Stock insuffisant pour " + inventoryPort.nomMatiere(matId)
                                + " (dispo: " + stock + ", demandé: " + aSortir + ")");
            }

            inventoryPort.diminuerStockAvecVerrou(matId, aSortir);
            inventoryPort.enregistrerMouvementSortieProduction(
                    matId, aSortir, cmd.productionId(), cmd.userId());

            ligne.confirmerMagasinier(deja + aSortir);

            events.publishEvent(StockExitedForProductionEvent.of(
                    cmd.productionId(), matId,
                    inventoryPort.nomMatiere(matId), aSortir, cmd.userId()));

            sorties.add(new LigneResultat(
                    matId,
                    inventoryPort.nomMatiere(matId),
                    aSortir,
                    false // flag farinage renseigné côté UI via GetMaterialsToSupply
            ));
        }

        productionPort.save(production);
        return new Result(cmd.productionId(), sorties);
    }
}
