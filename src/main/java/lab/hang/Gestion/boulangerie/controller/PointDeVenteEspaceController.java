package lab.hang.Gestion.boulangerie.controller;

import lab.hang.Gestion.boulangerie.dto.CommandeDTO;
import lab.hang.Gestion.boulangerie.exception.UserNotAuthenticatedException;
import lab.hang.Gestion.boulangerie.model.Commande;
import lab.hang.Gestion.boulangerie.model.PointDeVente;
import lab.hang.Gestion.boulangerie.model.User;
import lab.hang.Gestion.boulangerie.repository.CommandeRepository;
import lab.hang.Gestion.boulangerie.repository.LivraisonRepository;
import lab.hang.Gestion.boulangerie.repository.PointDeVenteRepository;
import lab.hang.Gestion.boulangerie.service.CommandeService;
import lab.hang.Gestion.boulangerie.service.ProduitService;
import lab.hang.Gestion.boulangerie.service.ReclamationService;
import lab.hang.Gestion.boulangerie.service.UserService;
import lab.hang.Gestion.boulangerie.service.VersementService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Espace self-service pour un utilisateur lié à un PointDeVente : consultation du solde
 * (livré - versé), de la commande du jour, des réclamations, et création d'une nouvelle
 * commande si la production du jour est déjà lancée (pas de modification rétroactive).
 */
@Controller
@RequestMapping("/mon-espace")
@PreAuthorize("hasRole('POINT_DE_VENTE')")
public class PointDeVenteEspaceController {

    private final UserService userService;
    private final VersementService versementService;
    private final ReclamationService reclamationService;
    private final CommandeRepository commandeRepository;
    private final LivraisonRepository livraisonRepository;
    private final CommandeService commandeService;
    private final ProduitService produitService;
    private final PointDeVenteRepository pointDeVenteRepository;

    public PointDeVenteEspaceController(UserService userService,
                                        VersementService versementService,
                                        ReclamationService reclamationService,
                                        CommandeRepository commandeRepository,
                                        LivraisonRepository livraisonRepository,
                                        CommandeService commandeService,
                                        ProduitService produitService,
                                        PointDeVenteRepository pointDeVenteRepository) {
        this.userService = userService;
        this.versementService = versementService;
        this.reclamationService = reclamationService;
        this.commandeRepository = commandeRepository;
        this.livraisonRepository = livraisonRepository;
        this.commandeService = commandeService;
        this.produitService = produitService;
        this.pointDeVenteRepository = pointDeVenteRepository;
    }

    private PointDeVente getPointDeVenteConnecte() {
        User user = userService.getCurrentUser();
        // user.getPointDeVente() est un proxy Hibernate lazy : .getId() est sûr (déjà en
        // mémoire dans le proxy), mais accéder à .getNom() plus tard dans la vue échouerait
        // (session fermée, spring.jpa.open-in-view=false) — on recharge donc l'entité à plat.
        if (user.getPointDeVente() == null) {
            throw new UserNotAuthenticatedException("Aucun point de vente n'est associé à ce compte. Contactez l'administrateur.");
        }
        return pointDeVenteRepository.findById(user.getPointDeVente().getId())
                .orElseThrow(() -> new UserNotAuthenticatedException("Point de vente introuvable."));
    }

    @GetMapping
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public String dashboard(Model model) {
        PointDeVente pdv = getPointDeVenteConnecte();
        LocalDate today = LocalDate.now();

        double solde = versementService.calculerSolde(pdv.getId());
        List<Commande> commandesDuJour = commandeRepository.findByDateCommandeAndProcessed(today, false)
                .stream().filter(c -> c.getPointDeVente() != null && c.getPointDeVente().getId().equals(pdv.getId()))
                .toList();
        boolean productionDemarreeAujourdhui = commandeRepository.findByDateCommandeAndProcessed(today, true)
                .stream().anyMatch(c -> c.getPointDeVente() != null && c.getPointDeVente().getId().equals(pdv.getId()));

        // Matérialiser ce dont la vue a besoin pendant que la session Hibernate est encore
        // ouverte (spring.jpa.open-in-view=false ferme la session avant le rendu Thymeleaf).
        record CommandeResume(LocalDate date, int nbProduits, double coutTotal) {}
        List<CommandeResume> commandesDuJourResume = commandesDuJour.stream()
                .map(c -> new CommandeResume(c.getDateCommande(), c.getProduitsCommandes().size(), c.getCoutTotal()))
                .toList();

        model.addAttribute("pointDeVente", pdv);
        model.addAttribute("solde", solde);
        model.addAttribute("commandesDuJour", commandesDuJourResume);
        model.addAttribute("productionDemarreeAujourdhui", productionDemarreeAujourdhui);
        model.addAttribute("livraisons", livraisonRepository.findByPointDeVenteIdOrderByDateLivraisonDesc(pdv.getId()));
        model.addAttribute("versements", versementService.getVersementsByPointDeVente(pdv.getId()));
        model.addAttribute("reclamations", reclamationService.getByPointDeVente(pdv.getId()));
        return "mon-espace/dashboard";
    }

    @GetMapping("/commande/new")
    public String nouvelleCommandeForm(Model model) {
        model.addAttribute("pointDeVente", getPointDeVenteConnecte());
        model.addAttribute("produits", produitService.getAllProduits());
        return "mon-espace/commande-new";
    }

    @PostMapping("/commande")
    public String creerCommande(@RequestParam Map<String, String> formParams,
                                RedirectAttributes ra) {
        PointDeVente pdv = getPointDeVenteConnecte();
        try {
            CommandeDTO dto = new CommandeDTO();
            dto.setDateCommande(LocalDate.now());
            dto.setPointDeVenteId(pdv.getId());
            Map<Long, Integer> produitsCommandes = new java.util.HashMap<>();
            formParams.forEach((key, value) -> {
                if (key.startsWith("produitsCommandes[") && value != null && !value.isBlank()) {
                    Long produitId = Long.valueOf(key.substring("produitsCommandes[".length(), key.length() - 1));
                    int quantite = Integer.parseInt(value);
                    if (quantite > 0) produitsCommandes.put(produitId, quantite);
                }
            });
            if (produitsCommandes.isEmpty()) {
                ra.addFlashAttribute("errorMessage", "Sélectionnez au moins un produit.");
                return "redirect:/mon-espace/commande/new";
            }
            dto.setProduitsCommandes(produitsCommandes);
            commandeService.createCommande(dto);
            ra.addFlashAttribute("successMessage", "Commande envoyée avec succès.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Erreur lors de la création de la commande : " + e.getMessage());
        }
        return "redirect:/mon-espace";
    }

    @PostMapping("/reclamations")
    public String creerReclamation(@RequestParam String message, RedirectAttributes ra) {
        PointDeVente pdv = getPointDeVenteConnecte();
        try {
            reclamationService.creer(pdv.getId(), message, userService.getCurrentUser());
            ra.addFlashAttribute("successMessage", "Réclamation envoyée. L'administration vous répondra prochainement.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Erreur : " + e.getMessage());
        }
        return "redirect:/mon-espace";
    }
}
