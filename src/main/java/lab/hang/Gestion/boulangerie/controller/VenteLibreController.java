package lab.hang.Gestion.boulangerie.controller;

import lab.hang.Gestion.boulangerie.dto.CreateVenteLibreRequest;
import lab.hang.Gestion.boulangerie.dto.ProductionDTO;
import lab.hang.Gestion.boulangerie.dto.ProduitDTO;
import lab.hang.Gestion.boulangerie.dto.VenteLibreDTO;
import lab.hang.Gestion.boulangerie.model.Production;
import lab.hang.Gestion.boulangerie.service.PointDeVenteService;
import lab.hang.Gestion.boulangerie.service.ProductionService;
import lab.hang.Gestion.boulangerie.service.ProduitService;
import lab.hang.Gestion.boulangerie.service.VenteLibreService;
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
@RequestMapping("/ventes-libres")
public class VenteLibreController {

    private final VenteLibreService venteLibreService;
    private final ProductionService productionService;
    private final ProduitService produitService;
    private final PointDeVenteService pointDeVenteService;

    public VenteLibreController(VenteLibreService venteLibreService,
                                ProductionService productionService,
                                ProduitService produitService,
                                PointDeVenteService pointDeVenteService) {
        this.venteLibreService = venteLibreService;
        this.productionService = productionService;
        this.produitService = produitService;
        this.pointDeVenteService = pointDeVenteService;
    }

    @GetMapping
    public String listVentesLibres(Model model) {
        model.addAttribute("ventesLibres", venteLibreService.getAllVentesLibres());
        return "vente-libre/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        LocalDate today = LocalDate.now();
        List<Production> productions = new ArrayList<>();
        productions.addAll(productionService.getProductionsByDate(today));
        productions.addAll(productionService.getProductionsByDate(today.minusDays(1)));

        model.addAttribute("productions", productions);
        model.addAttribute("venteLibreRequest", new CreateVenteLibreRequest());
        model.addAttribute("guichets", pointDeVenteService.getAllGuichetsActifs());
        return "vente-libre/create";
    }

    @PostMapping("/save")
    public String saveVenteLibre(@ModelAttribute CreateVenteLibreRequest request,
                                 RedirectAttributes redirectAttributes) {
        try {
            VenteLibreDTO vente = venteLibreService.createVenteLibre(request);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Vente libre enregistrée avec succès. Montant : " + vente.getMontantTotal() + " XAF");
            return "redirect:/ventes-libres/" + vente.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Erreur : " + e.getMessage());
            return "redirect:/ventes-libres/new";
        }
    }

    @GetMapping("/{id}")
    public String showDetails(@PathVariable Long id, Model model) {
        VenteLibreDTO vente = venteLibreService.getVenteLibreById(id);
        model.addAttribute("vente", vente);
        return "vente-libre/details";
    }

    @GetMapping("/production/{productionId}")
    public String listByProduction(@PathVariable Long productionId, Model model) {
        model.addAttribute("ventesLibres", venteLibreService.getVentesLibresByProduction(productionId));
        model.addAttribute("production", productionService.getProductionById(productionId));
        return "vente-libre/list";
    }

    /** Endpoint AJAX — retourne les produits restants d'une production (même format que livraisons). */
    @GetMapping("/ajax/production/{productionId}/produits")
    @ResponseBody
    public Map<String, Object> getProduitsRestants(@PathVariable Long productionId) {
        Map<String, Object> response = new HashMap<>();
        try {
            ProductionDTO production = productionService.getProductionById(productionId);
            Map<Long, ProduitDTO> nomsProduits = new HashMap<>();
            for (Long produitId : production.getProduitsRestants().keySet()) {
                nomsProduits.put(produitId, produitService.getProduitById(produitId));
            }
            response.put("produitsRestants", production.getProduitsRestants());
            response.put("nomsProduits", nomsProduits);
        } catch (Exception e) {
            response.put("error", e.getMessage());
        }
        return response;
    }
}
