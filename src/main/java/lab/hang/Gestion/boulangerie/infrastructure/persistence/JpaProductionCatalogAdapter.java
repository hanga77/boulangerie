package lab.hang.Gestion.boulangerie.infrastructure.persistence;

import lab.hang.Gestion.boulangerie.application.production.ProductionCatalogPort;
import lab.hang.Gestion.boulangerie.model.Commande;
import lab.hang.Gestion.boulangerie.model.MatierePremiere;
import lab.hang.Gestion.boulangerie.model.Produit;
import lab.hang.Gestion.boulangerie.repository.CommandeRepository;
import lab.hang.Gestion.boulangerie.repository.ProduitRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class JpaProductionCatalogAdapter implements ProductionCatalogPort {

    private final CommandeRepository commandeRepo;
    private final ProduitRepository produitRepo;

    public JpaProductionCatalogAdapter(CommandeRepository commandeRepo,
                                       ProduitRepository produitRepo) {
        this.commandeRepo = commandeRepo;
        this.produitRepo = produitRepo;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Integer> produitsCommandesDuJour(LocalDate date) {
        Map<Long, Integer> agregat = new HashMap<>();
        List<Commande> commandes = commandeRepo.findByDateCommandeAndProcessed(date, false);
        // aussi les déjà processées du jour si on relance ? non — seulement non traitées
        if (commandes.isEmpty()) {
            commandes = commandeRepo.findByDateCommande(date);
        }
        for (Commande c : commandes) {
            if (c.isProcessed()) continue;
            if (c.getProduitsCommandes() == null) continue;
            for (Map.Entry<Produit, Integer> e : c.getProduitsCommandes().entrySet()) {
                agregat.merge(e.getKey().getId(), e.getValue(), Integer::sum);
            }
        }
        return agregat;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Double> matieresNecessaires(Long produitId, int quantite) {
        Produit p = produitRepo.findById(produitId)
                .orElseThrow(() -> new IllegalArgumentException("Produit introuvable: " + produitId));
        Map<Long, Double> result = new HashMap<>();
        if (p.getMatieresPremieres() == null) return result;
        for (Map.Entry<MatierePremiere, Double> e : p.getMatieresPremieres().entrySet()) {
            result.merge(e.getKey().getId(), e.getValue() * quantite, Double::sum);
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProduitQuota> produitsAvecQuotaVenteLibre() {
        List<ProduitQuota> list = new ArrayList<>();
        for (Produit p : produitRepo.findAll()) {
            if (p.getQuantiteVenteLibreJournaliere() > 0) {
                list.add(new ProduitQuota(p.getId(), p.getQuantiteVenteLibreJournaliere()));
            }
        }
        return list;
    }

    @Override
    @Transactional
    public void marquerCommandesTraitees(LocalDate date, Long productionId, Long userId) {
        // production_id FK = ancienne Production ; on marque processed sans lier la V2
        List<Commande> commandes = commandeRepo.findByDateCommandeAndProcessed(date, false);
        for (Commande c : commandes) {
            c.setProcessed(true);
            commandeRepo.save(c);
        }
    }
}
