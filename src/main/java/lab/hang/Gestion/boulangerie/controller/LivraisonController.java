package lab.hang.Gestion.boulangerie.controller;

import lab.hang.Gestion.boulangerie.dto.CreateLivraisonRequest;
import lab.hang.Gestion.boulangerie.dto.LivraisonDTO;
import lab.hang.Gestion.boulangerie.dto.ProductionDTO;
import lab.hang.Gestion.boulangerie.dto.ProduitDTO;
import lab.hang.Gestion.boulangerie.model.Production;
import lab.hang.Gestion.boulangerie.service.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/livraisons")
public class LivraisonController {
    private final LivraisonService livraisonService;
    private final ProductionService productionService;
    private final ProduitService produitService;


    public LivraisonController(LivraisonService livraisonService,
                               ProductionService productionService, ProduitService produitService) {
        this.livraisonService = livraisonService;
        this.productionService = productionService;

        this.produitService = produitService;
    }

    @GetMapping
    public String listLivraisons(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        Page<LivraisonDTO> livraisonDTOS = livraisonService.getAllLivraisons(PageRequest.of(page, size));
        model.addAttribute("livraisons", livraisonDTOS.getContent() );
        model.addAttribute("currentPage", page);                // Page actuelle
        model.addAttribute("totalPages", livraisonDTOS.getTotalPages()); // Nombre total de pages
        model.addAttribute("size", size);
        return "livraisons/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        // Récupérer les productions du jour et de la veille
        LocalDate today = LocalDate.now();
        List<Production> productions = new ArrayList<>();
        productions.addAll(productionService.getProductionsByDate(today));
        productions.addAll(productionService.getProductionsByDate(today.minusDays(1)));

        model.addAttribute("productions", productions);
        model.addAttribute("livraisonRequest", new CreateLivraisonRequest());
        return "livraisons/create";
    }

    @PostMapping("/save")
    public String saveLivraison(@ModelAttribute CreateLivraisonRequest request,
                                RedirectAttributes redirectAttributes) {
        try {
            LivraisonDTO livraison = livraisonService.createLivraison(request);
            redirectAttributes.addFlashAttribute("success",
                    "Livraison créée avec succès");
            return "redirect:/livraisons/" + livraison.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Erreur lors de la création de la livraison: " + e.getMessage());
            return "redirect:/livraisons/new";
        }
    }

    @GetMapping("/{id}")
    public String showLivraisonDetails(@PathVariable Long id, Model model) {
        LivraisonDTO livraison = livraisonService.getLivraisonById(id);
        model.addAttribute("livraison", livraison);
        return "livraisons/details";
    }

    @GetMapping("/production/{productionId}")
    public String listLivraisonsByProduction(@PathVariable Long productionId, Model model) {
        List<LivraisonDTO> livraisons = livraisonService.getLivraisonsByProduction(productionId);
        ProductionDTO production = productionService.getProductionById(productionId);

        model.addAttribute("livraisons", livraisons);
        model.addAttribute("production", production);
        return "livraisons/list-by-production";
    }

    @GetMapping("/search")
    public String searchLivraisons(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                   Model model) {
        List<LivraisonDTO> livraisons = livraisonService.getLivraisonsByDateRange(startDate, endDate);
        model.addAttribute("livraisons", livraisons);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        return "livraisons/list";
    }


    @GetMapping("/ajax/production/{productionId}/produits")
    @ResponseBody
    public Map<String, Object> getProduitsRestants(@PathVariable Long productionId) {
        Map<String, Object> response = new HashMap<>();

        try {
            ProductionDTO production = productionService.getProductionById(productionId);

            // Récupérer les noms des produits
            Map<Long, ProduitDTO> nomsProduits = new HashMap<>();
            for (Map.Entry<Long, Integer> entry : production.getProduitsRestants().entrySet()) {
                ProduitDTO produit = produitService.getProduitById(entry.getKey());
                nomsProduits.put(entry.getKey(), produit);
            }

            // Ajouter des informations de débogage
            response.put("dateProduction", production.getDateProduction());
            response.put("produitsRestants", production.getProduitsRestants());
            response.put("produitsProduits", production.getProduitsProduits());
            response.put("nomsProduits", nomsProduits); // Ajouter les noms des produits

            // Vérifier si les maps sont null ou vides
            if (production.getProduitsRestants() == null) {
                response.put("error", "produitsRestants is null");
            } else if (production.getProduitsRestants().isEmpty()) {
                response.put("error", "produitsRestants is empty");
            }

            return response;


        } catch (Exception e) {
            response.put("error", "Error: " + e.getMessage());
            response.put("stackTrace", e.getStackTrace());
            return response;
        }
    }

    @PostMapping("/{id}/enregistrer-revenu")
    public String enregistrerRevenuLivraison(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            LivraisonDTO livraison = livraisonService.getLivraisonById(id);
            livraisonService.enregistrerRevenuLivraison(id, livraison.getMontantTotal());
            redirectAttributes.addFlashAttribute("success", "Revenu de la livraison enregistré avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de l'enregistrement du revenu: " + e.getMessage());
        }
        return "redirect:/livraisons/" + id;
    }
}