package lab.hang.Gestion.boulangerie.controller;

import lab.hang.Gestion.boulangerie.dto.CommandeDTO;
import lab.hang.Gestion.boulangerie.dto.ProductionDTO;
import lab.hang.Gestion.boulangerie.dto.ProduitDTO;
import lab.hang.Gestion.boulangerie.model.*;
import lab.hang.Gestion.boulangerie.repository.UserRepository;
import lab.hang.Gestion.boulangerie.service.*;
import lab.hang.Gestion.boulangerie.model.IncidentProduction;
import lab.hang.Gestion.boulangerie.model.TypeIncident;
import lab.hang.Gestion.boulangerie.service.IncidentProductionService;
import lab.hang.Gestion.boulangerie.service.AppSettingsService;
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
    private final IncidentProductionService incidentProductionService;
    private final AppSettingsService appSettingsService;

    public ProductionController(CommandeService commandeService, ProductionService productionService, MatierePremiereService matierePremiereService, UserService userService, ProduitService produitService, IncidentProductionService incidentProductionService, AppSettingsService appSettingsService) {
        this.commandeService = commandeService;
        this.productionService = productionService;
        this.matierePremiereService = matierePremiereService;
        this.userService = userService;
        this.produitService = produitService;
        this.incidentProductionService = incidentProductionService;
        this.appSettingsService = appSettingsService;
    }


    @PreAuthorize("hasRole('BOULANGER')")
    @GetMapping("/passer-a-la-production")
    public String afficherPageProduction(Model model) {
        List<CommandeDTO> commandeDTOS = commandeService.getCommandesNonTraitees();

        if (commandeDTOS.isEmpty()) {
            return "redirect:/production";
        }

        model.addAttribute("dateActuelle", LocalDate.now());
        model.addAttribute("commandes", commandeDTOS);
        return "production/passer-a-la-production";
    }

    @PreAuthorize("hasRole('BOULANGER')")
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
        model.addAttribute("seuil", appSettingsService.getSeuilIncident());

        return "production/confirm";
    }

    @PreAuthorize("hasRole('BOULANGER')")
    @PostMapping("/confirmer-production")
    public String confirmerProduction(@RequestParam LocalDate date, Model model) {
        // 1. Récupérer l'utilisateur actuellement connecté
        User user = userService.getCurrentUser();

        // 2. Démarrer la production (cette méthode calcule les quantités nécessaires en interne)
        ProductionDTO productionDTO = productionService.startProduction(date, user);

        // 3. Ajouter les détails de la production au modèle
        model.addAttribute("production", productionDTO);

        // 4. Rediriger vers une page de confirmation
        model.addAttribute("seuil", appSettingsService.getSeuilIncident());
        return "production/confirm";
    }

    @PreAuthorize("hasRole('BOULANGER')")
    @PostMapping("/valider-production")
    public String validerProduction(@RequestParam Map<String, String> formData,
                                    RedirectAttributes ra) {
        ProductionDTO productionDTO = new ProductionDTO();
        Map<Long, Double> quantitesReelles = new HashMap<>();
        Map<Long, Integer> produitsProduits = new HashMap<>();

        formData.forEach((key, value) -> {
            if (key.startsWith("quantitesReellesMatieres[")) {
                Long id = Long.valueOf(key.substring(key.indexOf("[") + 1, key.indexOf("]")));
                quantitesReelles.put(id, Double.valueOf(value));
            } else if (key.startsWith("quantitesReellesProduits[")) {
                Long id = Long.valueOf(key.substring(key.indexOf("[") + 1, key.indexOf("]")));
                produitsProduits.put(id, Integer.valueOf(value));
            }
        });

        productionDTO.setId(Long.valueOf(formData.get("productionId")));
        productionDTO.setQuantitesReellesUtilisees(quantitesReelles);
        productionDTO.setProduitsProduits(produitsProduits);
        productionService.updateProduction(productionDTO);

        if ("true".equals(formData.get("incidentSignaler"))) {
            try {
                Long productionId = Long.valueOf(formData.get("productionId"));
                ProductionDTO prod = productionService.getProductionById(productionId);
                Production productionEntity = new Production();
                productionEntity.setId(prod.getId());
                productionEntity.setDateProduction(prod.getDateProduction());

                TypeIncident type = TypeIncident.valueOf(formData.get("incidentType"));
                double quantitePerdue = Double.parseDouble(
                        formData.getOrDefault("incidentQuantitePerdue", "0"));
                String cause = formData.getOrDefault("incidentCause", "");
                User boulanger = userService.getCurrentUser();

                MatierePremiere matiere = null;
                String matiereIdStr = formData.get("incidentMatiereId");
                if (matiereIdStr != null && !matiereIdStr.isBlank()) {
                    matiere = matierePremiereService.getMatierePremiereById(Long.valueOf(matiereIdStr));
                }

                Produit produit = null;
                String produitIdStr = formData.get("incidentProduitId");
                if (produitIdStr != null && !produitIdStr.isBlank()) {
                    produit = produitService.getProduitEntityById(Long.valueOf(produitIdStr));
                }

                incidentProductionService.creerIncident(
                        productionEntity, type, produit, matiere, quantitePerdue, cause, boulanger);

                ra.addFlashAttribute("successMessage", "Production validée. Incident enregistré.");
            } catch (Exception e) {
                ra.addFlashAttribute("warningMessage",
                        "Production validée mais erreur lors de l'enregistrement de l'incident : " + e.getMessage());
            }
        }

        return "redirect:/production";
    }

    @GetMapping("/incidents")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String listIncidents(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin,
            @RequestParam(required = false) String type,
            Model model) {

        if (debut == null) debut = LocalDate.now().withDayOfMonth(1);
        if (fin == null) fin = LocalDate.now();

        TypeIncident typeIncident = null;
        if (type != null && !type.isBlank()) {
            try { typeIncident = TypeIncident.valueOf(type); } catch (IllegalArgumentException ignored) {}
        }

        var incidents = incidentProductionService.getAllIncidents(debut, fin, typeIncident);
        long avecImpactStock = incidents.stream().filter(IncidentProduction::isStockAjuste).count();

        model.addAttribute("incidents", incidents);
        model.addAttribute("debut", debut);
        model.addAttribute("fin", fin);
        model.addAttribute("typeSelectionne", type);
        model.addAttribute("typesIncident", TypeIncident.values());
        model.addAttribute("totalIncidents", incidents.size());
        model.addAttribute("avecImpactStock", avecImpactStock);
        model.addAttribute("sansImpactStock", incidents.size() - avecImpactStock);

        return "production/incidents";
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
        model.addAttribute("incidents",
                incidentProductionService.getIncidentsByProduction(productionId));

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