package lab.hang.Gestion.boulangerie.application.finance;

import lab.hang.Gestion.boulangerie.application.production.ProductionPersistencePort;
import lab.hang.Gestion.boulangerie.application.sales.SalesPersistencePort;
import lab.hang.Gestion.boulangerie.domain.production.ProductionLigneMatiere;
import lab.hang.Gestion.boulangerie.domain.production.ProductionV2;
import lab.hang.Gestion.boulangerie.domain.sales.LivraisonV2;
import lab.hang.Gestion.boulangerie.domain.sales.VenteLibreV2;
import lab.hang.Gestion.boulangerie.model.MatierePremiere;
import lab.hang.Gestion.boulangerie.repository.MatierePremiereRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Marge d'une production V2 : CA (livraisons + caisse) − coût matières.
 * Les charges fixes ne sont pas ventilées par lot (coût de structure).
 */
@Service
public class ProductionMargeService {

    private final ProductionPersistencePort productionPort;
    private final SalesPersistencePort salesPort;
    private final MatierePremiereRepository matiereRepo;

    public ProductionMargeService(ProductionPersistencePort productionPort,
                                  SalesPersistencePort salesPort,
                                  MatierePremiereRepository matiereRepo) {
        this.productionPort = productionPort;
        this.salesPort = salesPort;
        this.matiereRepo = matiereRepo;
    }

    public record LigneCout(Long matiereId, String nom, double quantite, double prixUnitaire, double cout) {}

    public record MargeProduction(
            Long productionId,
            String statut,
            double coutMatieres,
            double caLivraisons,
            double caCaisse,
            double caTotal,
            double marge,
            boolean rentable,
            List<LigneCout> detailCouts,
            int nbLivraisons,
            int nbVentes
    ) {}

    public MargeProduction calculer(Long productionId) {
        ProductionV2 p = productionPort.getById(productionId);

        List<LigneCout> details = new ArrayList<>();
        double coutMatieres = 0;
        for (ProductionLigneMatiere lm : p.getLignesMatiere()) {
            double qte = quantiteConsommee(lm);
            double prix = matiereRepo.findById(lm.getMatierePremiereId())
                    .map(MatierePremiere::getPrixUnitaire).orElse(0.0);
            double cout = qte * prix;
            coutMatieres += cout;
            String nom = matiereRepo.findById(lm.getMatierePremiereId())
                    .map(MatierePremiere::getNom).orElse("#" + lm.getMatierePremiereId());
            details.add(new LigneCout(lm.getMatierePremiereId(), nom, qte, prix, cout));
        }

        List<LivraisonV2> livs = salesPort.findLivraisonsByProduction(productionId);
        List<VenteLibreV2> ventes = salesPort.findVentesByProduction(productionId);

        double caLiv = livs.stream().mapToDouble(LivraisonV2::getMontantTotal).sum();
        double caCaisse = ventes.stream().mapToDouble(VenteLibreV2::getMontantTotal).sum();
        double caTotal = caLiv + caCaisse;
        double marge = caTotal - coutMatieres;

        return new MargeProduction(
                productionId,
                p.getStatut() != null ? p.getStatut().name() : "?",
                coutMatieres,
                caLiv,
                caCaisse,
                caTotal,
                marge,
                marge >= 0,
                details,
                livs.size(),
                ventes.size()
        );
    }

    private static double quantiteConsommee(ProductionLigneMatiere lm) {
        if (lm.getQuantiteBoulanger() != null && lm.getQuantiteBoulanger() > 0) {
            return lm.getQuantiteBoulanger();
        }
        if (lm.getQuantiteMagasinier() != null && lm.getQuantiteMagasinier() > 0) {
            return lm.getQuantiteMagasinier();
        }
        return lm.getQuantiteTheorique();
    }
}
