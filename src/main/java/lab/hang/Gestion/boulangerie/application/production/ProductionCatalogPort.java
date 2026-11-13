package lab.hang.Gestion.boulangerie.application.production;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ProductionCatalogPort {

    record ProduitQuota(Long id, int quota) {}

    Map<Long, Integer> produitsCommandesDuJour(LocalDate date);

    Map<Long, Double> matieresNecessaires(Long produitId, int quantite);

    List<ProduitQuota> produitsAvecQuotaVenteLibre();

    void marquerCommandesTraitees(LocalDate date, Long productionId, Long userId);
}
