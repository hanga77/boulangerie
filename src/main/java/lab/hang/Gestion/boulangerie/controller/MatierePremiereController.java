package lab.hang.Gestion.boulangerie.controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lab.hang.Gestion.boulangerie.exception.SoldeInsuffisantException;
import lab.hang.Gestion.boulangerie.exception.StockInsuffisantException;
import lab.hang.Gestion.boulangerie.model.MatierePremiere;
import lab.hang.Gestion.boulangerie.model.Production;
import lab.hang.Gestion.boulangerie.model.StockMovement;
import lab.hang.Gestion.boulangerie.repository.ProductionRepository;
import lab.hang.Gestion.boulangerie.service.MatierePremiereService;
import lab.hang.Gestion.boulangerie.service.StockService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/matieres-premieres")
public class MatierePremiereController {

    private static final Logger log = LoggerFactory.getLogger(MatierePremiereController.class);

    private final MatierePremiereService matierePremiereService;
    private final StockService stockService;
    private final ProductionRepository productionRepository;

    public MatierePremiereController(MatierePremiereService matierePremiereService,
                                     StockService stockService,
                                     ProductionRepository productionRepository) {
        this.matierePremiereService = matierePremiereService;
        this.stockService = stockService;
        this.productionRepository = productionRepository;
    }

    @GetMapping
    public String listMatierePremieres(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        Page<MatierePremiere> matierePremieres = matierePremiereService.getAllMatierePremieres(PageRequest.of(page, size));
        model.addAttribute("matieresPremieres", matierePremieres.getContent());
        model.addAttribute("currentPage", page);                // Page actuelle
        model.addAttribute("totalPages", matierePremieres.getTotalPages()); // Nombre total de pages
        model.addAttribute("size", size);
        return "matiere-premerie/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("matierePremiere", new MatierePremiere());
        return "matiere-premerie/create";
    }

    @PostMapping
    public String saveMatierePremiere(@ModelAttribute("matierePremiere") MatierePremiere matierePremiere) {
        matierePremiereService.saveMatierePremiere(matierePremiere);
        return "redirect:/matieres-premieres";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("matierePremiere", matierePremiereService.getMatierePremiereById(id));
        return "matiere-premerie/edit";
    }

    @PostMapping("/update/{id}")
    public String updateMatierePremiere(@PathVariable Long id, @ModelAttribute("matierePremiere") MatierePremiere matierePremiere) {
        matierePremiereService.updateMatierePremiere(id, matierePremiere);
        return "redirect:/matieres-premieres";
    }

    @GetMapping("/delete/{id}")
    public String deleteMatierePremiere(@PathVariable Long id) {
        matierePremiereService.deleteMatierePremiere(id);
        return "redirect:/matieres-premieres";
    }

    @GetMapping("/search")
    public String searchMatierePremieres(@RequestParam String nom, Model model) {
        model.addAttribute("matieresPremieres", matierePremiereService.searchMatierePremieres(nom));
        model.addAttribute("searchQuery", nom);
        return "matiere-premerie/search";
    }


    @GetMapping("/add-stock")
    public String showAddStockForm(@RequestParam(required = false) Long id, Model model) {
        if (id == null) return "redirect:/matieres-premieres/mouvements-stock";
        model.addAttribute("matierePremiere", matierePremiereService.getMatierePremiereById(id));
        return "matiere-premerie/add-stock";
    }

    @PostMapping("/add-stock")
    public String addStock(@RequestParam Long id,
                           @RequestParam double quantite,
                           @RequestParam double prixUnitaire,
                           @RequestParam(required = false) Double quantiteCommandee,
                           @RequestParam(required = false) Double quantiteAvariee,
                           RedirectAttributes redirectAttributes) {
        try {
            stockService.addStock(id, quantite, prixUnitaire, quantiteCommandee, quantiteAvariee);
        } catch (Exception e) {
            log.error("Erreur lors de l'ajout du stock", e);
            return "redirect:/matieres-premieres/add-stock?id=" + id;
        }
        return "redirect:/matieres-premieres";
    }

    @GetMapping("/remove-stock")
    public String showRemoveStockForm(@RequestParam Long id, Model model) {
        model.addAttribute("matierePremiere", matierePremiereService.getMatierePremiereById(id));
        return "matiere-premerie/remove-stock";
    }

    @PostMapping("/remove-stock")
    public String removeStock(@RequestParam Long id,
                              @RequestParam double quantite,
                              RedirectAttributes redirectAttributes) {
        try {
            stockService.removeStock(id, quantite);
            redirectAttributes.addFlashAttribute("success", "Stock retiré avec succès");
        } catch (Exception e) {
            log.error("Erreur lors du retrait du stock", e);
            redirectAttributes.addFlashAttribute("error", "Erreur : " + e.getMessage());
            return "redirect:/matieres-premieres/remove-stock?id=" + id;
        }
        return "redirect:/matieres-premieres";
    }

    @GetMapping("/mouvements-stock")
    public String gestionMouvementsStock(Model model) {
        model.addAttribute("matieresPremieres", matierePremiereService.getAllMatierePremieres());
        model.addAttribute("mouvementsStock", stockService.getAllMovementsDesc());

        LocalDate today = LocalDate.now();
        List<Production> productions = productionRepository.findByDateProductionBetween(today.minusDays(30), today);
        productions.sort(Comparator.comparing(Production::getDateProduction).reversed());
        model.addAttribute("productions", productions);

        return "matiere-premerie/gestion";
    }

    @PostMapping("/mouvements-stock")
    public String enregistrerMouvementStock(
            @RequestParam Long matierePremiereId,
            @RequestParam double quantite,
            @RequestParam String type,
            @RequestParam(required = false) Double prixUnitaire,
            @RequestParam(required = false) Double quantiteCommandee,
            @RequestParam(required = false) Double quantiteAvariee,
            @RequestParam(required = false) Long productionId,
            @RequestParam(required = false) String motif,
            RedirectAttributes redirectAttributes) {

        try {
            switch (type) {
                case "ENTREE" -> {
                    if (prixUnitaire == null || prixUnitaire <= 0) {
                        return "redirect:/matieres-premieres/mouvements-stock";
                    }
                    stockService.addStock(matierePremiereId, quantite, prixUnitaire, quantiteCommandee, quantiteAvariee);
                }
                case "SORTIE" -> {
                    if (productionId == null) {
                        redirectAttributes.addFlashAttribute("error",
                                "Une production de référence est obligatoire pour une sortie de stock.");
                        return "redirect:/matieres-premieres/mouvements-stock";
                    }
                    stockService.removeStockForProduction(matierePremiereId, quantite, productionId);
                    redirectAttributes.addFlashAttribute("success", "Sortie de stock enregistrée et liée à la production.");
                }
                case "RETOUR" -> {
                    stockService.returnStock(matierePremiereId, quantite,
                            motif != null && !motif.isBlank() ? motif : "RETOUR MANUEL");
                    redirectAttributes.addFlashAttribute("success", "Retour de stock enregistré avec succès.");
                }
                case "PERTE" -> {
                    stockService.lostStock(matierePremiereId, quantite,
                            motif != null && !motif.isBlank() ? motif : "Perte non spécifiée");
                    redirectAttributes.addFlashAttribute("success", "Perte enregistrée avec succès.");
                }
                default -> {
                    redirectAttributes.addFlashAttribute("error", "Type de mouvement inconnu : " + type);
                    return "redirect:/matieres-premieres/mouvements-stock";
                }
            }
        } catch (SoldeInsuffisantException e) {
            redirectAttributes.addFlashAttribute("warning",
                    "Mouvement enregistré (achat à crédit) : " + e.getMessage());
        } catch (StockInsuffisantException e) {
            redirectAttributes.addFlashAttribute("error", "Stock insuffisant : " + e.getMessage());
            return "redirect:/matieres-premieres/mouvements-stock";
        } catch (Exception e) {
            log.error("Erreur lors de l'enregistrement du mouvement de stock", e);
            redirectAttributes.addFlashAttribute("error", "Une erreur est survenue : " + e.getMessage());
            return "redirect:/matieres-premieres/mouvements-stock";
        }

        return "redirect:/matieres-premieres/mouvements-stock";
    }
}
