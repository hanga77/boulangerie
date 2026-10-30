package lab.hang.Gestion.boulangerie.controller;

import lab.hang.Gestion.boulangerie.domain.sales.LivraisonV2;
import lab.hang.Gestion.boulangerie.domain.sales.VenteLibreV2;
import lab.hang.Gestion.boulangerie.infrastructure.persistence.LivraisonV2Repository;
import lab.hang.Gestion.boulangerie.infrastructure.persistence.ProductionV2Repository;
import lab.hang.Gestion.boulangerie.infrastructure.persistence.VenteLibreV2Repository;
import lab.hang.Gestion.boulangerie.model.StockMovement;
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

/**
 * Traçabilité cycle V2 — sorties stock, livraisons, ventes (phases 4–5).
 */
@Controller
@RequestMapping("/cycle")
public class CycleTraceabilityController {

    private final StockMovementRepository stockMovementRepo;
    private final LivraisonV2Repository livraisonV2Repo;
    private final VenteLibreV2Repository venteLibreV2Repo;
    private final ProductionV2Repository productionV2Repo;

    public CycleTraceabilityController(StockMovementRepository stockMovementRepo,
                                       LivraisonV2Repository livraisonV2Repo,
                                       VenteLibreV2Repository venteLibreV2Repo,
                                       ProductionV2Repository productionV2Repo) {
        this.stockMovementRepo = stockMovementRepo;
        this.livraisonV2Repo = livraisonV2Repo;
        this.venteLibreV2Repo = venteLibreV2Repo;
        this.productionV2Repo = productionV2Repo;
    }

    @GetMapping("/sorties")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','MAGASINIER')")
    public String sorties(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long productionV2Id,
            Model model) {

        LocalDate jour = date != null ? date : LocalDate.now();
        List<StockMovement> sorties = stockMovementRepo.findSortiesV2(productionV2Id, jour);
        double totalQte = sorties.stream().mapToDouble(StockMovement::getQuantite).sum();

        model.addAttribute("date", jour);
        model.addAttribute("productionV2Id", productionV2Id);
        model.addAttribute("sorties", sorties);
        model.addAttribute("totalQte", totalQte);
        model.addAttribute("productions", productionV2Repo.findAll());
        return "cycle/sorties";
    }

    @GetMapping("/livraisons-v2")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String livraisonsV2(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin,
            Model model) {

        if (debut == null) debut = LocalDate.now().withDayOfMonth(1);
        if (fin == null) fin = LocalDate.now();

        List<LivraisonV2> livraisons =
                livraisonV2Repo.findByDateLivraisonBetweenOrderByDateLivraisonDescIdDesc(debut, fin);
        double totalMontant = livraisons.stream().mapToDouble(LivraisonV2::getMontantTotal).sum();

        model.addAttribute("debut", debut);
        model.addAttribute("fin", fin);
        model.addAttribute("livraisons", livraisons);
        model.addAttribute("totalMontant", totalMontant);
        model.addAttribute("nb", livraisons.size());
        return "cycle/livraisons-v2";
    }

    /** Phase 5 P0 — historique global des ventes caisse / vente libre V2. */
    @GetMapping("/ventes-v2")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','CAISSIER')")
    public String ventesV2(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin,
            Model model) {

        if (debut == null) debut = LocalDate.now().withDayOfMonth(1);
        if (fin == null) fin = LocalDate.now();

        List<VenteLibreV2> ventes =
                venteLibreV2Repo.findByDateVenteBetweenOrderByDateVenteDescIdDesc(debut, fin);
        double totalMontant = ventes.stream().mapToDouble(VenteLibreV2::getMontantTotal).sum();
        long nbEspeces = ventes.stream().filter(v -> "ESPECES".equalsIgnoreCase(v.getMoyenPaiement())).count();
        long nbMobile = ventes.stream().filter(v -> "MOBILE_MONEY".equalsIgnoreCase(v.getMoyenPaiement())).count();

        model.addAttribute("debut", debut);
        model.addAttribute("fin", fin);
        model.addAttribute("ventes", ventes);
        model.addAttribute("totalMontant", totalMontant);
        model.addAttribute("nb", ventes.size());
        model.addAttribute("nbEspeces", nbEspeces);
        model.addAttribute("nbMobile", nbMobile);
        return "cycle/ventes-v2";
    }
}
