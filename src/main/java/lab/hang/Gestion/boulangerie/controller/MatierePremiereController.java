package lab.hang.Gestion.boulangerie.controller;


import jakarta.servlet.http.HttpServletResponse;
import lab.hang.Gestion.boulangerie.dto.StockReportDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lab.hang.Gestion.boulangerie.exception.SoldeInsuffisantException;
import lab.hang.Gestion.boulangerie.exception.StockInsuffisantException;
import lab.hang.Gestion.boulangerie.model.MatierePremiere;
import lab.hang.Gestion.boulangerie.model.StockMovement;
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
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/matieres-premieres")
public class MatierePremiereController {

    private static final Logger log = LoggerFactory.getLogger(MatierePremiereController.class);

    private final MatierePremiereService matierePremiereService;
    private final StockService stockService;

    public MatierePremiereController(MatierePremiereService matierePremiereService, StockService stockService) {
        this.matierePremiereService = matierePremiereService;
        this.stockService = stockService;
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
        return "redirect:/matieres-premieres";
    }


    @GetMapping("/mouvements-stock")
    public String gestionMouvementsStock(Model model) {
        // Récupérer la liste des matières premières
        List<MatierePremiere> matieresPremieres = matierePremiereService.getAllMatierePremieres();
        model.addAttribute("matieresPremieres", matieresPremieres);

        // Récupérer les mouvements de stock pour aujourd'hui
        LocalDate today = LocalDate.now();
        List<StockMovement> mouvementsStock = stockService.getStockMovementsByDate(today);
        model.addAttribute("mouvementsStock", mouvementsStock);

        return "matiere-premerie/gestion";
    }

    @PostMapping("/mouvements-stock")
    public String enregistrerMouvementStock(
            @RequestParam Long matierePremiereId,
            @RequestParam double quantite,
            @RequestParam String type,
            @RequestParam(required = false) Double prixUnitaire,
            RedirectAttributes redirectAttributes) {

        try {
            if (type.equals("ENTREE")) {
                if (prixUnitaire == null || prixUnitaire <= 0) {
                    redirectAttributes.addFlashAttribute("error",
                            "Le prix unitaire est requis et doit être supérieur à 0 pour une entrée de stock");
                    return "redirect:/matieres-premieres/mouvements-stock";
                }
                stockService.addStock(matierePremiereId, quantite, prixUnitaire);
                redirectAttributes.addFlashAttribute("success",
                        "Entrée de stock enregistrée avec succès" +
                                (prixUnitaire != null ? " (Prix unitaire: " + prixUnitaire + "€)" : ""));
            } else if (type.equals("SORTIE")) {
                stockService.removeStock(matierePremiereId, quantite);
                redirectAttributes.addFlashAttribute("success", "Sortie de stock enregistrée avec succès");
            } else if (type.equals("RETOUR")) {
                stockService.returnStock(matierePremiereId, quantite, "RETOUR MANUEL");
                redirectAttributes.addFlashAttribute("success", "Retour de stock enregistré avec succès");
            }
        } catch (SoldeInsuffisantException e) {
            redirectAttributes.addFlashAttribute("warning",
                    "Mouvement enregistré mais attention : " + e.getMessage() +
                            ". Le mouvement a été enregistré en tant qu'achat à crédit.");
        } catch (StockInsuffisantException e) {
            redirectAttributes.addFlashAttribute("error",
                    "Erreur : " + e.getMessage());
            return "redirect:/matieres-premieres/mouvements-stock";
        } catch (Exception e) {
            log.error("Erreur lors de l'enregistrement du mouvement de stock", e);
            redirectAttributes.addFlashAttribute("error",
                    "Une erreur est survenue : " + e.getMessage());
            return "redirect:/matieres-premieres/mouvements-stock";
        }

        return "redirect:/matieres-premieres/mouvements-stock";
    }
}
