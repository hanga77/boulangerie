package lab.hang.Gestion.boulangerie.controller;

import lab.hang.Gestion.boulangerie.model.MatierePremiere;
import lab.hang.Gestion.boulangerie.model.StockMovement;
import lab.hang.Gestion.boulangerie.service.LivraisonService;
import lab.hang.Gestion.boulangerie.service.MatierePremiereService;
import lab.hang.Gestion.boulangerie.service.ProductionService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/rapports")
public class RapportController {

    private final MatierePremiereService matierePremiereService;
    private final ProductionService productionService;
    private final LivraisonService livraisonService;

    public RapportController(MatierePremiereService matierePremiereService,
                             ProductionService productionService,
                             LivraisonService livraisonService) {
        this.matierePremiereService = matierePremiereService;
        this.productionService = productionService;
        this.livraisonService = livraisonService;
    }

    @GetMapping("/stocks")
    public String rapportStocks(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin,
            Model model) {

        if (debut == null) debut = LocalDate.now().withDayOfMonth(1);
        if (fin == null) fin = LocalDate.now();

        List<MatierePremiere> matieres = matierePremiereService.getAllMatierePremieres();
        List<StockMovement> mouvements = matierePremiereService.getStockMovementsByDateRange(debut, fin);

        long nbCritiques = matieres.stream()
                .filter(m -> m.getStock() <= m.getStockMinimum())
                .count();
        double totalValeur = matieres.stream()
                .mapToDouble(m -> m.getStock() * m.getPrixUnitaire())
                .sum();

        model.addAttribute("matieres", matieres);
        model.addAttribute("mouvements", mouvements);
        model.addAttribute("nbCritiques", nbCritiques);
        model.addAttribute("totalValeur", totalValeur);
        model.addAttribute("debut", debut);
        model.addAttribute("fin", fin);
        return "rapports/stocks";
    }

    @GetMapping("/production")
    public String rapportProduction(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin,
            Model model) {

        if (debut == null) debut = LocalDate.now().withDayOfMonth(1);
        if (fin == null) fin = LocalDate.now();

        var productions = productionService.getProductionsByDateRange(debut, fin);
        double coutTotal = productionService.calculerCoutTotalProduction(debut, fin);

        model.addAttribute("productions", productions);
        model.addAttribute("coutTotal", coutTotal);
        model.addAttribute("debut", debut);
        model.addAttribute("fin", fin);
        return "rapports/production";
    }

    @GetMapping("/livraisons")
    public String rapportLivraisons(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin,
            Model model) {

        if (debut == null) debut = LocalDate.now().withDayOfMonth(1);
        if (fin == null) fin = LocalDate.now();

        var livraisons = livraisonService.getLivraisonsByDateRange(debut, fin);
        double chiffreAffaires = livraisons.stream()
                .mapToDouble(l -> l.getMontantTotal())
                .sum();

        model.addAttribute("livraisons", livraisons);
        model.addAttribute("chiffreAffaires", chiffreAffaires);
        model.addAttribute("debut", debut);
        model.addAttribute("fin", fin);
        return "rapports/livraisons";
    }
}
