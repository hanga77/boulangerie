package lab.hang.Gestion.boulangerie.application.sales;

import lab.hang.Gestion.boulangerie.domain.sales.LivraisonV2;
import lab.hang.Gestion.boulangerie.domain.sales.VenteLibreV2;

import java.util.List;

public interface SalesPersistencePort {
    LivraisonV2 saveLivraison(LivraisonV2 livraison);
    VenteLibreV2 saveVente(VenteLibreV2 vente);
    List<LivraisonV2> findLivraisonsByProduction(Long productionV2Id);
    List<VenteLibreV2> findVentesByProduction(Long productionV2Id);
    int countLivraisonsByProduction(Long productionV2Id);
    int countVentesByDate(java.time.LocalDate date);
    void crediterCompte(Long compteId, double montant);
}
