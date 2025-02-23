package lab.hang.Gestion.boulangerie.controller;

import lab.hang.Gestion.boulangerie.model.MatierePremiere;
import lab.hang.Gestion.boulangerie.model.StockMovement;
import lab.hang.Gestion.boulangerie.service.StockService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/mouvements")
public class StockMovementController {
    private final StockService stockService;

    public StockMovementController(StockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping
    public String listMouvements(
            @RequestParam(required = false) LocalDate dateDebut,
            @RequestParam(required = false) LocalDate dateFin,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        // Si dates non spécifiées, utiliser aujourd'hui
        if (dateDebut == null) dateDebut = LocalDate.now();
        if (dateFin == null) dateFin = dateDebut;

        Page<StockMovement> mouvementsPage = stockService.getMouvementsByDateRange(
                dateDebut, dateFin, PageRequest.of(page, size));

        model.addAttribute("mouvements", mouvementsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", mouvementsPage.getTotalPages());
        model.addAttribute("dateDebut", dateDebut);
        model.addAttribute("dateFin", dateFin);

        return "matiere-premerie/stock-list";
    }

    @GetMapping("/print")
    public String imprimerMouvements(
            @RequestParam LocalDate date,
            Model model) {

        List<StockMovement> mouvements = stockService.getStockMovementsByDate(date);

        double totalEntrees = mouvements.stream()
                .filter(m -> m.getType().equals("ENTREE"))
                .mapToDouble(StockMovement::getQuantite)
                .sum();

        double totalSorties = mouvements.stream()
                .filter(m -> m.getType().equals("SORTIE"))
                .mapToDouble(StockMovement::getQuantite)
                .sum();

        model.addAttribute("mouvements", mouvements);
        model.addAttribute("date", date);
        model.addAttribute("totalEntrees", totalEntrees);
        model.addAttribute("totalSorties", totalSorties);

        return "matiere-premerie/print";
    }

    @GetMapping("/stock-summary")
    public String getStockSummary(@RequestParam LocalDate date, Model model) {
        Map<MatierePremiere, StockService.StockSummary> stockSummaryMap = stockService.getStockSummary(date);

        model.addAttribute("stockSummaryMap", stockSummaryMap);
        model.addAttribute("date", date);

        return "matiere-premerie/stock-summary"; // Vue à créer
    }
}