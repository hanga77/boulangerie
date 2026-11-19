package lab.hang.Gestion.boulangerie.application.production;

import lab.hang.Gestion.boulangerie.domain.production.ProductionV2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;

/**
 * Lance la production du jour.
 *
 * Dès cet instant, le théorique matières est figé et devient la
 * feuille de sortie du magasinier (recettes des commandes + quotas
 * vente libre + farinage du lot).
 */
@Service
public class StartDailyProductionUseCase {

    private final ProductionCatalogPort catalogPort;
    private final ProductionPersistencePort persistencePort;

    public StartDailyProductionUseCase(ProductionCatalogPort catalogPort,
                                       ProductionPersistencePort persistencePort) {
        this.catalogPort = catalogPort;
        this.persistencePort = persistencePort;
    }

    /**
     * @param farinageMatiereId  matière utilisée pour le farinage / fleurage du lot (ex. farine)
     * @param farinageQuantite   quantité de farinage à ajouter au théorique — sortie magasin obligatoire
     */
    public record Command(
            LocalDate dateProduction,
            Long userId,
            Long farinageMatiereId,
            Double farinageQuantite
    ) {}

    @Transactional
    public ProductionV2 execute(Command cmd) {
        if (persistencePort.existsForDate(cmd.dateProduction())) {
            throw new IllegalStateException(
                    "Une production existe déjà pour le " + cmd.dateProduction());
        }

        ProductionV2 production = new ProductionV2(cmd.dateProduction(), cmd.userId());

        // 1) Produits commandés → théorique recettes
        Map<Long, Integer> produitsCommandes = catalogPort.produitsCommandesDuJour(cmd.dateProduction());
        for (Map.Entry<Long, Integer> e : produitsCommandes.entrySet()) {
            production.ajouterLigneProduit(e.getKey(), e.getValue());
            Map<Long, Double> matieres = catalogPort.matieresNecessaires(e.getKey(), e.getValue());
            matieres.forEach(production::ajouterOuCumulerMatiere);
        }

        // 2) Quotas vente libre journalière
        for (var produit : catalogPort.produitsAvecQuotaVenteLibre()) {
            production.ajouterLigneProduit(produit.id(), produit.quota());
            Map<Long, Double> matieres = catalogPort.matieresNecessaires(produit.id(), produit.quota());
            matieres.forEach(production::ajouterOuCumulerMatiere);
        }

        // 3) Farinage du lot — inclus dans le théorique magasinier (obligatoire si renseigné)
        if (cmd.farinageMatiereId() != null
                && cmd.farinageQuantite() != null
                && cmd.farinageQuantite() > 0) {
            production.ajouterOuCumulerMatiere(cmd.farinageMatiereId(), cmd.farinageQuantite());
        }

        if (production.getLignesMatieres().isEmpty()) {
            throw new IllegalStateException(
                    "Aucune matière théorique : pas de commandes ni de farinage pour le "
                            + cmd.dateProduction());
        }

        ProductionV2 saved = persistencePort.save(production);
        catalogPort.marquerCommandesTraitees(cmd.dateProduction(), saved.getId(), cmd.userId());
        return saved;
    }
}
