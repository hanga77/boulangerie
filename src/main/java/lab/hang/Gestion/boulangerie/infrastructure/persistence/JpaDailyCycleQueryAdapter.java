package lab.hang.Gestion.boulangerie.infrastructure.persistence;

import lab.hang.Gestion.boulangerie.application.production.DailyCycleQueryPort;
import lab.hang.Gestion.boulangerie.domain.production.ProductionStatus;
import lab.hang.Gestion.boulangerie.domain.production.ProductionV2;
import lab.hang.Gestion.boulangerie.repository.CommandeRepository;
import lab.hang.Gestion.boulangerie.repository.LivraisonRepository;
import lab.hang.Gestion.boulangerie.repository.VenteLibreRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;

@Component
public class JpaDailyCycleQueryAdapter implements DailyCycleQueryPort {

    private final CommandeRepository commandeRepo;
    private final ProductionV2Repository productionRepo;
    private final LivraisonRepository livraisonRepo;
    private final VenteLibreRepository venteLibreRepo;

    public JpaDailyCycleQueryAdapter(CommandeRepository commandeRepo,
                                     ProductionV2Repository productionRepo,
                                     LivraisonRepository livraisonRepo,
                                     VenteLibreRepository venteLibreRepo) {
        this.commandeRepo = commandeRepo;
        this.productionRepo = productionRepo;
        this.livraisonRepo = livraisonRepo;
        this.venteLibreRepo = venteLibreRepo;
    }

    @Override
    public int countCommandes(LocalDate date) {
        return commandeRepo.countByDateCommande(date);
    }

    @Override
    public int countCommandesTraitees(LocalDate date) {
        return commandeRepo.findByDateCommandeAndProcessed(date, true).size();
    }

    @Override
    public Optional<ProductionV2> findProductionDuJour(LocalDate date) {
        return productionRepo.findByDateProductionWithLignes(date);
    }

    @Override
    public int countLivraisonsPourProduction(Long productionId) {
        return 0;
    }

    @Override
    public int countVentesGuichet(LocalDate date) {
        try {
            return (int) venteLibreRepo.findAll().stream()
                    .filter(v -> v.getDateVente() != null && v.getDateVente().equals(date))
                    .count();
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public boolean isJourCloture(LocalDate date) {
        return productionRepo.findByDateProductionWithLignes(date)
                .map(p -> p.getStatut() == ProductionStatus.CLOTUREE)
                .orElse(false);
    }
}
