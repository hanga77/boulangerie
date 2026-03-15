package lab.hang.Gestion.boulangerie.controller;

import lab.hang.Gestion.boulangerie.dto.ProduitDTO;
import lab.hang.Gestion.boulangerie.exception.ProduitNotFoundException;
import lab.hang.Gestion.boulangerie.model.MatierePremiere;
import lab.hang.Gestion.boulangerie.service.MatierePremiereService;
import lab.hang.Gestion.boulangerie.service.ProduitService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/produits")
public class ProduitController {

    private final ProduitService produitService;
    private final MatierePremiereService matierePremiereService;

    public ProduitController(ProduitService produitService, MatierePremiereService matierePremiereService) {
        this.produitService = produitService;
        this.matierePremiereService = matierePremiereService;
    }

    @GetMapping
    public String listProduits(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        // Récupérer tous les produits sous forme de DTO
        Page<ProduitDTO> produits = produitService.getAllProduits(PageRequest.of(page, size));

        // Charger toutes les matières premières en une seule requête (évite N+1)
        Map<Long, MatierePremiere> matiereCache = matierePremiereService.getAllMatierePremieres()
                .stream()
                .collect(Collectors.toMap(MatierePremiere::getId, Function.identity()));

        Map<Long, Double> farineQuantities = new HashMap<>();
        for (ProduitDTO produit : produits.getContent()) {
            double farineQuantity = produit.getMatieresPremieres().entrySet().stream()
                    .filter(entry -> {
                        MatierePremiere matiere = matiereCache.get(entry.getKey());
                        return matiere != null && matiere.getNom().toLowerCase().contains("farine");
                    })
                    .map(Map.Entry::getValue)
                    .findFirst()
                    .orElse(0.0);
            farineQuantities.put(produit.getId(), farineQuantity);
        }

        // Ajouter les produits et les quantités de farine au modèle
        model.addAttribute("produits", produits.getContent());
        model.addAttribute("farineQuantities", farineQuantities);
        model.addAttribute("currentPage", page);                // Page actuelle
        model.addAttribute("totalPages", produits.getTotalPages()); // Nombre total de pages
        model.addAttribute("size", size);

        return "produits/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        List<MatierePremiere> matieresPremieres = matierePremiereService.getAllMatierePremieres();
        model.addAttribute("produitDTO", new ProduitDTO());
        model.addAttribute("matieresPremieres", matieresPremieres);
        return "produits/create";
    }

    @PostMapping
    public String saveProduit(@Valid @ModelAttribute("produitDTO") ProduitDTO produitDTO,
                              BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("matieresPremieres", matierePremiereService.getAllMatierePremieres());
            return "produits/create";
        }
        produitService.saveProduit(produitDTO);
        return "redirect:/produits";
    }

    @PostMapping("/update/{id}")
    public String updateProduit(@PathVariable Long id,
                                @Valid @ModelAttribute("produitDTO") ProduitDTO produitDTO,
                                BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("matieresPremieres", matierePremiereService.getAllMatierePremieres());
            return "produits/edit";
        }
        try {
            produitService.updateProduit(id, produitDTO);
            return "redirect:/produits";
        } catch (ProduitNotFoundException e) {
            return "redirect:/produits?error=Produit non trouvé";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        try {
            ProduitDTO produitDTO = produitService.getProduitById(id);
            List<MatierePremiere> matieresPremieres = matierePremiereService.getAllMatierePremieres();
            model.addAttribute("produitDTO", produitDTO);
            model.addAttribute("matieresPremieres", matieresPremieres);
            return "produits/edit";
        } catch (ProduitNotFoundException e) {
            return "redirect:/produits?error=Produit non trouvé";
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteProduit(@PathVariable Long id) {
        try {
            produitService.deleteProduit(id);
            return "redirect:/produits";
        } catch (ProduitNotFoundException e) {
            return "redirect:/produits?error=Produit non trouvé";
        }
    }

    @GetMapping("/search")
    public String searchProduits(@RequestParam String nom, Model model) {
        model.addAttribute("produits", produitService.searchProduits(nom));
        return "produits/list";
    }
}