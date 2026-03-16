package lab.hang.Gestion.boulangerie.controller;

import lab.hang.Gestion.boulangerie.dto.CommandeDTO;
import lab.hang.Gestion.boulangerie.dto.ProductionDTO;
import lab.hang.Gestion.boulangerie.dto.ProduitDTO;
import lab.hang.Gestion.boulangerie.model.*;
import lab.hang.Gestion.boulangerie.repository.UserRepository;
import lab.hang.Gestion.boulangerie.service.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/production")
public class ProductionController {

    private final CommandeService commandeService;
    private final ProductionService productionService;
    private final MatierePremiereService matierePremiereService;
    private final UserService userService;
    private final ProduitService produitService;

    public ProductionController(CommandeService commandeService, ProductionService productionService, MatierePremiereService matierePremiereService, UserService userService, ProduitService produitService) {
        this.commandeService = commandeService;
        this.productionService = productionService;
        this.matierePremiereService = matierePremiereService;
        this.userService = userService;
        this.produitService = produitService;
    }


    @GetMapping("/passer-a-la-production")
    public String afficherPageProduction(Model model) {
        List<CommandeDTO> commandeDTOS = commandeService.getCommandesNonTraitees();

        if (commandeDTOS.isEmpty()){
            return "redirect:/production";
        }

        model.addAttribute("dateActuelle", LocalDate.now());
        // Afficher une page avec un formulaire pour passer à la production
        return "production/passer-a-la-production";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'BOULANGER')")
    @PostMapping("/passer-a-la-production")
    public String passerALaProduction(@RequestParam LocalDate date, Model model) {
        // 1. Récupérer l'utilisateur actuellement connecté
        User user = userService.getCurrentUser();

        // 2. Démarrer la production
        ProductionDTO productionDTO = productionService.startProduction(date, user);


        // 3. Vérifier les stocks avant de confirmer la production
        boolean stocksSuffisants = productionService.verifierStocksSuffisants(productionDTO);

        if (!stocksSuffisants) {
            model.addAttribute("warning", "Attention : Les stocks sont insuffisants pour certaines matières premières. Veuillez réapprovisionner.");
        }

        // 4. Ajouter les produits et matières premières détaillés
        Map<Long, ProduitDTO> produitsMap = new HashMap<>();
        for (Long produitId : productionDTO.getProduitsProduits().keySet()) {
            produitsMap.put(produitId, produitService.getProduitById(produitId));
        }
        model.addAttribute("produits", produitsMap);

        Map<Long, MatierePremiere> matieresMap = new HashMap<>();
        for (Long matiereId : productionDTO.getMatieresPremieresUtilisees().keySet()) {
            matieresMap.put(matiereId, matierePremiereService.getMatierePremiereById(matiereId));
        }
        model.addAttribute("matieres", matieresMap);

        // 5. Ajouter les détails de la production au modèle
        model.addAttribute("production", productionDTO);

        return "production/confirm";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'BOULANGER')")
    @PostMapping("/confirmer-production")
    public String confirmerProduction(@RequestParam LocalDate date, Model model) {
        // 1. Récupérer l'utilisateur actuellement connecté
        User user = userService.getCurrentUser();

        // 2. Démarrer la production (cette méthode calcule les quantités nécessaires en interne)
        ProductionDTO productionDTO = productionService.startProduction(date, user);

        // 3. Ajouter les détails de la production au modèle
        model.addAttribute("production", productionDTO);

        // 4. Rediriger vers une page de confirmation
        return "production/confirm";
    }

    // In your controller
    @PreAuthorize("hasAnyRole('ADMIN', 'BOULANGER')")
    @PostMapping("/valider-production")
    public String validerProduction(@RequestParam Map<String, String> formData) {
        ProductionDTO productionDTO = new ProductionDTO();

        Map<Long, Double> quantitesReelles = new HashMap<>();
        Map<Long, Integer> produitsProduits = new HashMap<>();

        // Convert String keys to Long when processing form data
        formData.forEach((key, value) -> {
            if (key.startsWith("quantitesReellesMatieres[")) {
                String idStr = key.substring(key.indexOf("[") + 1, key.indexOf("]"));
                Long id = Long.valueOf(idStr);
                quantitesReelles.put(id, Double.valueOf(value));
            } else if (key.startsWith("quantitesReellesProduits[")) {
                String idStr = key.substring(key.indexOf("[") + 1, key.indexOf("]"));
                Long id = Long.valueOf(idStr);
                produitsProduits.put(id, Integer.valueOf(value));
            }
        });

        productionDTO.setId(Long.valueOf(formData.get("productionId")));
        productionDTO.setQuantitesReellesUtilisees(quantitesReelles);
        productionDTO.setProduitsProduits(produitsProduits);

        productionService.updateProduction(productionDTO);
        return "redirect:/production";
    }

    @GetMapping("/details/{productionId}")
    public String afficherDetailsProduction(@PathVariable Long productionId, Model model) {
        // 1. Récupérer la production
        ProductionDTO productionDTO = productionService.getProductionById(productionId);
        Map<Long, ProduitDTO> produitsMap = new HashMap<>();
        for (Long produitId : productionDTO.getProduitsProduits().keySet()) {
            produitsMap.put(produitId, produitService.getProduitById(produitId));
        }

        Map<Long, MatierePremiere> matieresMap = new HashMap<>();
        for (Long matiereId : productionDTO.getMatieresPremieresUtilisees().keySet()) {
            matieresMap.put(matiereId, matierePremiereService.getMatierePremiereById(matiereId));
        }

        // 2. Ajouter les détails de la production au modèle
        model.addAttribute("production", productionDTO);
        model.addAttribute("produits", produitsMap);
        model.addAttribute("matiere", matieresMap);

        // 3. Rediriger vers une page de détails
        return "production/details";
    }
    @PostMapping("/livrer-produits")
    public String livrerProduits(@RequestParam Long productionId,
                                 @RequestParam Map<Long, Integer> quantitesLivrees,
                                 Model model) {
        try {
            // 1. Récupérer la production
            ProductionDTO productionDTO = productionService.getProductionById(productionId);

            // 2. Mettre à jour les quantités restantes
            productionService.updateProduitsRestants(productionDTO, quantitesLivrees);

            // 3. Rediriger vers la page de détails de la production
            return "redirect:/production/details/" + productionId;
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "production/details"; // Retourner à la page de détails avec un message d'erreur
        }
    }

    @GetMapping
    public String getPreviousProductions(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        // Si les dates ne sont pas spécifiées, utiliser une période par défaut (par exemple, le mois en cours)
        if (startDate == null) {
            startDate = LocalDate.now().withDayOfMonth(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        // Récupérer la page de productions
        Page<Production> productionPage = productionService.getProductionsByDateRange(startDate, endDate,PageRequest.of(page, size));

        // Ajouter les quantités commandées à chaque production
        for (Production production : productionPage.getContent()) {
            List<CommandeDTO> commandes = commandeService.getCommandesByProductionId(production.getId());
            int quantiteCommandee = commandes.stream()
                    .flatMap(commande -> commande.getProduitsCommandes().entrySet().stream())
                    .filter(entry -> entry.getKey().equals(production.getProduitsProduits().keySet().iterator().next().getId()))
                    .mapToInt(Map.Entry::getValue)
                    .sum();

            production.setQuantiteCommandee(quantiteCommandee);
        }

        // Ajouter toutes les informations nécessaires au modèle
        model.addAttribute("productions", productionPage.getContent());
        model.addAttribute("currentPage", page);                // Page actuelle
        model.addAttribute("totalPages", productionPage.getTotalPages()); // Nombre total de pages
        model.addAttribute("size", size);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "production/previous";
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/enregistrer-cout")
    public String enregistrerCoutProduction(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            productionService.enregistrerCoutProduction(id );
            redirectAttributes.addFlashAttribute("success", "Coût de production enregistré avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de l'enregistrement du coût: " + e.getMessage());
        }
        return "redirect:/production/details/" + id;
    }
}