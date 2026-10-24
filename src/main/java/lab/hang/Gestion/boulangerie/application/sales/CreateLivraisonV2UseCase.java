package lab.hang.Gestion.boulangerie.application.sales;

import lab.hang.Gestion.boulangerie.application.finance.FinancePort;
import lab.hang.Gestion.boulangerie.application.production.ProductionPersistencePort;
import lab.hang.Gestion.boulangerie.domain.production.ProductionLigneProduit;
import lab.hang.Gestion.boulangerie.domain.production.ProductionStatus;
import lab.hang.Gestion.boulangerie.domain.production.ProductionV2;
import lab.hang.Gestion.boulangerie.domain.sales.LivraisonV2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@Service
public class CreateLivraisonV2UseCase {

    private static final int AGE_MAX_JOURS = 2;

    private final ProductionPersistencePort productionPort;
    private final SalesPersistencePort salesPort;
    private final CatalogSalesPort catalogPort;
    private final FinancePort financePort;

    public CreateLivraisonV2UseCase(ProductionPersistencePort productionPort,
                                    SalesPersistencePort salesPort,
                                    CatalogSalesPort catalogPort,
                                    FinancePort financePort) {
        this.productionPort = productionPort;
        this.salesPort = salesPort;
        this.catalogPort = catalogPort;
        this.financePort = financePort;
    }

    /** produitId → (quantite, prixVente) */
    public record LigneCmd(int quantite, double prixVente) {}

    public record Command(
            Long productionV2Id,
            String nomClient,
            Long pointDeVenteId,
            Long userId,
            Map<Long, LigneCmd> lignes,
            boolean enregistrerRevenu
    ) {}

    @Transactional
    public LivraisonV2 execute(Command cmd) {
        ProductionV2 production = productionPort.getById(cmd.productionV2Id());

        if (production.getStatut() != ProductionStatus.VALIDEE
                && production.getStatut() != ProductionStatus.CLOTUREE) {
            throw new IllegalStateException(
                    "La production doit être VALIDEE avant livraison (statut: " + production.getStatut() + ")");
        }

        long age = ChronoUnit.DAYS.between(production.getDateProduction(), LocalDate.now());
        if (age < 0 || age > AGE_MAX_JOURS) {
            throw new IllegalArgumentException(
                    "Production trop ancienne (> " + AGE_MAX_JOURS + " j) — produits périmés");
        }

        if (cmd.lignes() == null || cmd.lignes().isEmpty()) {
            throw new IllegalArgumentException("Au moins une ligne produit est requise");
        }

        LivraisonV2 livraison = new LivraisonV2();
        livraison.setProductionV2Id(production.getId());
        livraison.setNomClient(cmd.nomClient() != null ? cmd.nomClient() : "Client");
        livraison.setPointDeVenteId(cmd.pointDeVenteId());
        livraison.setUserId(cmd.userId());
        livraison.setDateLivraison(LocalDate.now());

        for (Map.Entry<Long, LigneCmd> e : cmd.lignes().entrySet()) {
            if (e.getValue().quantite() <= 0) continue;
            ProductionLigneProduit ligneProd = production.ligneProduit(e.getKey())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Produit absent de la production: " + e.getKey()));
            ligneProd.debiterRestant(e.getValue().quantite());

            double prix = e.getValue().prixVente() > 0
                    ? e.getValue().prixVente()
                    : catalogPort.prixProduit(e.getKey());
            livraison.ajouterLigne(e.getKey(), e.getValue().quantite(), prix);
        }

        productionPort.save(production);
        LivraisonV2 saved = salesPort.saveLivraison(livraison);

        if (cmd.enregistrerRevenu() && saved.getMontantTotal() > 0) {
            var compte = financePort.comptePrincipal();
            financePort.enregistrerTransaction(
                    "VENTE",
                    saved.getMontantTotal(),
                    "Livraison V2 #" + saved.getId() + " — " + saved.getNomClient(),
                    compte.id()
            );
            // crédit compte : on utilise enregistrerTransaction + inverse debit via solde+
            // FinancePort n'a que debiterCompte — on ajoute crédit via adapter (voir JpaFinanceAdapter)
            salesPort.crediterCompte(compte.id(), saved.getMontantTotal());
            saved.setRevenuEnregistre(true);
            saved = salesPort.saveLivraison(saved);
        }

        return saved;
    }
}
