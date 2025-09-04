package lab.hang.Gestion.boulangerie.service;

import lab.hang.Gestion.boulangerie.model.Livraison;
import lab.hang.Gestion.boulangerie.model.Production;
import lab.hang.Gestion.boulangerie.model.Produit;
import lab.hang.Gestion.boulangerie.repository.KPIRepository;
import lab.hang.Gestion.boulangerie.repository.LivraisonRepository;
import lab.hang.Gestion.boulangerie.repository.ProductionRepository;
import lab.hang.Gestion.boulangerie.repository.StockMovementRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class KPIService {
    private final KPIRepository kpiRepository;
    private final ProductionRepository productionRepository;
    private final LivraisonRepository livraisonRepository;
    private final StockMovementRepository stockMovementRepository;

    public KPIService(KPIRepository kpiRepository,
                      ProductionRepository productionRepository,
                      LivraisonRepository livraisonRepository,
                      StockMovementRepository stockMovementRepository) {
        this.kpiRepository = kpiRepository;
        this.productionRepository = productionRepository;
        this.livraisonRepository = livraisonRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    public void calculerKPIsJournaliers() {
        LocalDate today = LocalDate.now();

        // Taux de marge par produit
        calculerTauxMargeProduits(today);

        // Rotation des stocks
        calculerRotationStocks(today);

        // Rentabilité par point de vente
        calculerRentabilitePointsVente(today);
    }

    private void calculerRentabilitePointsVente(LocalDate today) {
    }

    private void calculerRotationStocks(LocalDate today) {
    }

    private void calculerTauxMargeProduits(LocalDate date) {
        List<Production> productions = productionRepository.findByDateProduction(date);
        List<Livraison> livraisons = livraisonRepository.findByDateLivraison(date);

        /*for (Production production : productions) {
            for (Map.Entry<Produit, Integer> entry : production.getProduitsProduits().entrySet()) {
                Produit produit = entry.getKey();
                double coutProduction = production.getCoutUnitaire(produit);
                double prixVente = produit.getPrix();
                double tauxMarge = ((prixVente - coutProduction) / prixVente) * 100;

                sauvegarderKPI("TAUX_MARGE_" + produit.getNom(),
                        "MARGE", tauxMarge, date, "POURCENTAGE");
            }
        }*/
    }

    public Object getKPIsJournaliers() {
        return null;
    }

    public Object getKPIsGroupedByCategorie() {
        return null;
    }

    // Autres méthodes
}
