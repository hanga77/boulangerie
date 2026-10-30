package lab.hang.Gestion.boulangerie.application.sales;

import lab.hang.Gestion.boulangerie.application.finance.FinancePort;
import lab.hang.Gestion.boulangerie.application.finance.TransactionTypes;
import lab.hang.Gestion.boulangerie.application.production.ProductionPersistencePort;
import lab.hang.Gestion.boulangerie.domain.production.ProductionLigneProduit;
import lab.hang.Gestion.boulangerie.domain.production.ProductionStatus;
import lab.hang.Gestion.boulangerie.domain.production.ProductionV2;
import lab.hang.Gestion.boulangerie.domain.sales.VenteLibreV2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;

@Service
public class CreateVenteLibreV2UseCase {

    private final ProductionPersistencePort productionPort;
    private final SalesPersistencePort salesPort;
    private final CatalogSalesPort catalogPort;
    private final FinancePort financePort;

    public CreateVenteLibreV2UseCase(ProductionPersistencePort productionPort,
                                     SalesPersistencePort salesPort,
                                     CatalogSalesPort catalogPort,
                                     FinancePort financePort) {
        this.productionPort = productionPort;
        this.salesPort = salesPort;
        this.catalogPort = catalogPort;
        this.financePort = financePort;
    }

    public record Command(
            Long productionV2Id,
            Long userId,
            Long guichetId,
            String moyenPaiement,
            Map<Long, Integer> quantites
    ) {}

    @Transactional
    public VenteLibreV2 execute(Command cmd) {
        ProductionV2 production = productionPort.getById(cmd.productionV2Id());

        if (production.getStatut() != ProductionStatus.VALIDEE
                && production.getStatut() != ProductionStatus.CLOTUREE) {
            throw new IllegalStateException(
                    "La production doit être VALIDEE pour vendre (statut: " + production.getStatut() + ")");
        }

        LocalDate today = LocalDate.now();
        LocalDate d = production.getDateProduction();
        if (!d.equals(today) && !d.equals(today.minusDays(1))) {
            throw new IllegalArgumentException("Production hors fenêtre (jour ou veille uniquement)");
        }

        if (cmd.quantites() == null || cmd.quantites().isEmpty()) {
            throw new IllegalArgumentException("Aucune quantité saisie");
        }

        VenteLibreV2 vente = new VenteLibreV2();
        vente.setProductionV2Id(production.getId());
        vente.setUserId(cmd.userId());
        vente.setGuichetId(cmd.guichetId());
        vente.setMoyenPaiement(cmd.moyenPaiement() != null ? cmd.moyenPaiement() : "ESPECES");
        vente.setDateVente(LocalDate.now());

        for (Map.Entry<Long, Integer> e : cmd.quantites().entrySet()) {
            if (e.getValue() == null || e.getValue() <= 0) continue;
            ProductionLigneProduit ligne = production.ligneProduit(e.getKey())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Produit absent de la production: " + e.getKey()));
            ligne.debiterRestant(e.getValue());
            double prix = catalogPort.prixProduit(e.getKey());
            vente.ajouterLigne(e.getKey(), e.getValue(), prix);
        }

        if (vente.getLignes().isEmpty()) {
            throw new IllegalArgumentException("Aucune ligne valide");
        }

        productionPort.save(production);
        VenteLibreV2 saved = salesPort.saveVente(vente);

        var compte = financePort.comptePrincipal();
        String desc = "Vente caisse V2 #" + saved.getId()
                + " — " + saved.getMoyenPaiement();
        financePort.enregistrerTransaction(
                TransactionTypes.VENTE_CAISSE_V2,
                saved.getMontantTotal(),
                desc,
                compte.id());
        salesPort.crediterCompte(compte.id(), saved.getMontantTotal());

        return saved;
    }
}
