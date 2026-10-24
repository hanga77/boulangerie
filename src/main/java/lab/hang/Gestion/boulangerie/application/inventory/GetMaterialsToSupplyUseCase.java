package lab.hang.Gestion.boulangerie.application.inventory;

import lab.hang.Gestion.boulangerie.domain.production.ProductionLigneMatiere;
import lab.hang.Gestion.boulangerie.domain.production.ProductionStatus;
import lab.hang.Gestion.boulangerie.domain.production.ProductionV2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Dès que la production est lancée, le magasinier obtient la liste exacte
 * de ce qu'il doit fournir au boulanger (théorique recettes + farinage du lot).
 */
@Service
public class GetMaterialsToSupplyUseCase {

    private final ProductionPort productionPort;
    private final InventoryPort inventoryPort;
    /** IDs de matières considérées comme farinage (configurable). */
    private final Set<Long> farinageMatiereIds;

    public GetMaterialsToSupplyUseCase(ProductionPort productionPort,
                                       InventoryPort inventoryPort) {
        this(productionPort, inventoryPort, Set.of());
    }

    public GetMaterialsToSupplyUseCase(ProductionPort productionPort,
                                       InventoryPort inventoryPort,
                                       Set<Long> farinageMatiereIds) {
        this.productionPort = productionPort;
        this.inventoryPort = inventoryPort;
        this.farinageMatiereIds = farinageMatiereIds != null ? farinageMatiereIds : Set.of();
    }

    public record Command(Long productionId) {}

    public record Result(
            Long productionId,
            ProductionStatus statut,
            List<MatiereAFournir> lignes,
            boolean toutEstSorti,
            boolean stockGlobalSuffisant
    ) {}

    @Transactional(readOnly = true)
    public Result execute(Command cmd) {
        ProductionV2 production = productionPort.getById(cmd.productionId());

        if (production.getStatut() == ProductionStatus.PLANIFIEE) {
            throw new IllegalStateException(
                    "La production n'est pas encore lancée — pas de feuille de sortie");
        }

        List<MatiereAFournir> lignes = new ArrayList<>();
        boolean toutSorti = true;
        boolean stockOk = true;

        for (ProductionLigneMatiere ligne : production.getLignesMatieres()) {
            Long matId = ligne.getMatierePremiereId();
            double stock = inventoryPort.stockActuel(matId);
            boolean isFarinage = farinageMatiereIds.contains(matId);

            MatiereAFournir item = new MatiereAFournir(
                    matId,
                    inventoryPort.nomMatiere(matId),
                    inventoryPort.uniteMesure(matId),
                    ligne.getQuantiteTheorique(),
                    stock,
                    ligne.getQuantiteMagasinier(),
                    isFarinage
            );
            lignes.add(item);

            if (item.quantiteRestanteASortir() > 0) {
                toutSorti = false;
            }
            if (!item.stockSuffisant()) {
                stockOk = false;
            }
        }

        // Farinage en premier dans la liste pour que le magasinier ne l'oublie pas
        lignes.sort((a, b) -> Boolean.compare(b.farinage(), a.farinage()));

        return new Result(
                production.getId(),
                production.getStatut(),
                lignes,
                toutSorti,
                stockOk
        );
    }
}
