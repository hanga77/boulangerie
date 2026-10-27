package lab.hang.Gestion.boulangerie.application.production;

import lab.hang.Gestion.boulangerie.domain.production.ProductionLigneMatiere;
import lab.hang.Gestion.boulangerie.domain.production.ProductionLigneProduit;
import lab.hang.Gestion.boulangerie.domain.production.ProductionStatus;
import lab.hang.Gestion.boulangerie.domain.production.ProductionV2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Étape 3 du cycle : le boulanger saisit les quantités réellement produites
 * et éventuellement les quantités matières déclarées, puis valide la production.
 */
@Service
public class ValidateProductionQuantitiesUseCase {

    private final ProductionPersistencePort persistencePort;
    private final double seuilIncidentPourcent;

    public ValidateProductionQuantitiesUseCase(
            ProductionPersistencePort persistencePort,
            @Value("${boulangerie.production.seuil-incident-pourcent:10.0}") double seuilIncidentPourcent) {
        this.persistencePort = persistencePort;
        this.seuilIncidentPourcent = seuilIncidentPourcent;
    }

    /**
     * @param quantitesProduits  produitId → quantité réelle produite
     * @param quantitesMatieres  matiereId → quantité déclarée par le boulanger (optionnel)
     */
    public record Command(
            Long productionId,
            Long userId,
            Map<Long, Integer> quantitesProduits,
            Map<Long, Double> quantitesMatieres
    ) {}

    public record Result(ProductionV2 production, int nbIncidentsDetectes) {}

    @Transactional
    public Result execute(Command cmd) {
        ProductionV2 production = persistencePort.getById(cmd.productionId());

        if (production.getStatut() != ProductionStatus.EN_COURS) {
            throw new IllegalStateException(
                    "La production doit être EN_COURS pour être validée (statut actuel: "
                            + production.getStatut() + ")");
        }

        if (cmd.quantitesProduits() != null) {
            for (Map.Entry<Long, Integer> e : cmd.quantitesProduits().entrySet()) {
                ProductionLigneProduit ligne = production.ligneProduit(e.getKey())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Produit absent de la production: " + e.getKey()));
                ligne.validerQuantiteReelle(e.getValue());
            }
        }

        int incidents = 0;
        if (cmd.quantitesMatieres() != null) {
            for (Map.Entry<Long, Double> e : cmd.quantitesMatieres().entrySet()) {
                ProductionLigneMatiere ligne = production.ligneMatiere(e.getKey())
                        .orElseGet(() -> {
                            production.ajouterOuCumulerMatiere(e.getKey(), 0);
                            return production.ligneMatiere(e.getKey()).orElseThrow();
                        });
                ligne.declarerBoulanger(e.getValue());
                if (ligne.ecartPourcent() > seuilIncidentPourcent) {
                    incidents++;
                }
            }
        }

        production.valider();
        ProductionV2 saved = persistencePort.save(production);
        return new Result(saved, incidents);
    }
}
