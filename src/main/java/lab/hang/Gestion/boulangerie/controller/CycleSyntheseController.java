package lab.hang.Gestion.boulangerie.controller;

import lab.hang.Gestion.boulangerie.domain.production.ProductionLigneMatiere;
import lab.hang.Gestion.boulangerie.domain.production.ProductionLigneProduit;
import lab.hang.Gestion.boulangerie.domain.production.ProductionV2;
import lab.hang.Gestion.boulangerie.domain.sales.LivraisonV2;
import lab.hang.Gestion.boulangerie.domain.sales.VenteLibreV2;
import lab.hang.Gestion.boulangerie.infrastructure.persistence.LivraisonV2Repository;
import lab.hang.Gestion.boulangerie.infrastructure.persistence.ProductionV2Repository;
import lab.hang.Gestion.boulangerie.infrastructure.persistence.VenteLibreV2Repository;
import lab.hang.Gestion.boulangerie.model.StockMovement;
import lab.hang.Gestion.boulangerie.repository.CommandeRepository;
import lab.hang.Gestion.boulangerie.repository.StockMovementRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Phase 5 P1 — synthèse journalière : commandes, production, sorties, livraisons, ventes.
 */
@Controller
@RequestMapping("/cycle/synthese")
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
public class CycleSyntheseController {

    private final CommandeRepository commandeRepo;
    private final ProductionV2Repository productionRepo;
    private final LivraisonV2Repository livraisonRepo;
    private final VenteLibreV2Repository venteRepo;
    private final StockMovementRepository stockMovementRepo;

    public CycleSyntheseController(CommandeRepository commandeRepo,
                                   ProductionV2Repository productionRepo,
                                   LivraisonV2Repository livraisonRepo,
                                   VenteLibreV2Repository venteRepo,
                                   StockMovementRepository stockMovementRepo) {
        this.commandeRepo = commandeRepo;
        this.productionRepo = productionRepo;
        this.livraisonRepo = livraisonRepo;
        this.venteRepo = venteRepo;
        this.stockMovementRepo = stockMovementRepo;
    }

    @GetMapping
    public String synthese(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model) {

        LocalDate jour = date != null ? date : LocalDate.now();
        Optional<ProductionV2> optProd = productionRepo.findByDateProductionWithLignes(jour);

        int nbCommandes = commandeRepo.countByDateCommande(jour);
        int nbCommandesTraitees = commandeRepo.findByDateCommandeAndProcessed(jour, true).size();

        List<LivraisonV2> livraisons = List.of();
        List<VenteLibreV2> ventes = List.of();
        List<StockMovement> sorties = List.of();
        int qteProduitsPlanifies = 0;
        int qteProduitsRestants = 0;
        int nbMatieresConfirmees = 0;
        int nbMatieres = 0;
        double montantLivraisons = 0;
        double montantVentes = 0;
        double qteSorties = 0;

        if (optProd.isPresent()) {
            ProductionV2 p = optProd.get();
            for (ProductionLigneProduit l : p.getLignesProduits()) {
                qteProduitsPlanifies += l.getQuantiteReelle() > 0 ? l.getQuantiteReelle() : l.getQuantitePlanifiee();
                qteProduitsRestants += l.getQuantiteRestante();
            }
            nbMatieres = p.getLignesMatieres().size();
            for (ProductionLigneMatiere m : p.getLignesMatieres()) {
                if (m.getQuantiteMagasinier() != null) nbMatieresConfirmees++;
            }
            livraisons = livraisonRepo.findByProductionV2Id(p.getId());
            ventes = venteRepo.findByProductionV2Id(p.getId());
            sorties = stockMovementRepo.findSortiesV2(p.getId(), jour);
            montantLivraisons = livraisons.stream().mapToDouble(LivraisonV2::getMontantTotal).sum();
            montantVentes = ventes.stream().mapToDouble(VenteLibreV2::getMontantTotal).sum();
            qteSorties = sorties.stream().mapToDouble(StockMovement::getQuantite).sum();
        }

        model.addAttribute("date", jour);
        model.addAttribute("production", optProd.orElse(null));
        model.addAttribute("nbCommandes", nbCommandes);
        model.addAttribute("nbCommandesTraitees", nbCommandesTraitees);
        model.addAttribute("qteProduitsPlanifies", qteProduitsPlanifies);
        model.addAttribute("qteProduitsRestants", qteProduitsRestants);
        model.addAttribute("nbMatieres", nbMatieres);
        model.addAttribute("nbMatieresConfirmees", nbMatieresConfirmees);
        model.addAttribute("livraisons", livraisons);
        model.addAttribute("ventes", ventes);
        model.addAttribute("sorties", sorties);
        model.addAttribute("nbLivraisons", livraisons.size());
        model.addAttribute("nbVentes", ventes.size());
        model.addAttribute("montantLivraisons", montantLivraisons);
        model.addAttribute("montantVentes", montantVentes);
        model.addAttribute("montantTotal", montantLivraisons + montantVentes);
        model.addAttribute("qteSorties", qteSorties);
        model.addAttribute("nbSorties", sorties.size());

        return "cycle/synthese";
    }
}
