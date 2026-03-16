package lab.hang.Gestion.boulangerie.controller;

import lab.hang.Gestion.boulangerie.dto.CommandeDTO;
import lab.hang.Gestion.boulangerie.dto.ProduitDTO;
import lab.hang.Gestion.boulangerie.service.CommandeService;
import lab.hang.Gestion.boulangerie.service.PointDeVenteService;
import lab.hang.Gestion.boulangerie.service.ProduitService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/commandes")
public class CommandeController {

    private static final Logger log = LoggerFactory.getLogger(CommandeController.class);

    private final CommandeService commandeService;
    private final ProduitService produitService;
    private final PointDeVenteService pointDeVenteService;

    public CommandeController(CommandeService commandeService, ProduitService produitService,
                              PointDeVenteService pointDeVenteService) {
        this.commandeService = commandeService;
        this.produitService = produitService;
        this.pointDeVenteService = pointDeVenteService;
    }

    @GetMapping
    public String listCommandes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        Page<CommandeDTO> commandes = commandeService.getAllCommandes(PageRequest.of(page, size));

        model.addAttribute("commandes", commandes);
        model.addAttribute("currentPage", page);                // Page actuelle
        model.addAttribute("totalPages", commandes.getTotalPages()); // Nombre total de pages
        model.addAttribute("size", size);                        // Taille de la page
        return "commandes/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("commandeDTO", new CommandeDTO());
        model.addAttribute("produits", produitService.getAllProduits());
        model.addAttribute("pointsDeVente", pointDeVenteService.getPointsDeVenteActifs());
        model.addAttribute("guichets", pointDeVenteService.getAllGuichetsActifs());
        return "commandes/create";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        CommandeDTO commandeDTO = commandeService.getCommandeById(id);
        model.addAttribute("commandeDTO", commandeDTO);
        model.addAttribute("produits", produitService.getAllProduits());
        model.addAttribute("pointsDeVente", pointDeVenteService.getPointsDeVenteActifs());
        model.addAttribute("guichets", pointDeVenteService.getAllGuichetsActifs());
        return "commandes/edit";
    }

    @PostMapping
    public String saveCommande(@Valid @ModelAttribute("commandeDTO") CommandeDTO commandeDTO,
                               BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("produits", produitService.getAllProduits());
            model.addAttribute("pointsDeVente", pointDeVenteService.getPointsDeVenteActifs());
            model.addAttribute("guichets", pointDeVenteService.getAllGuichetsActifs());
            return "commandes/create";
        }
        commandeService.createCommande(commandeDTO);
        return "redirect:/commandes";
    }

    @PostMapping("/update/{id}")
    public String updateCommande(@PathVariable Long id,
                                 @Valid @ModelAttribute("commandeDTO") CommandeDTO commandeDTO,
                                 BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("produits", produitService.getAllProduits());
            model.addAttribute("pointsDeVente", pointDeVenteService.getPointsDeVenteActifs());
            model.addAttribute("guichets", pointDeVenteService.getAllGuichetsActifs());
            return "commandes/edit";
        }
        commandeService.updateCommande(id, commandeDTO);
        return "redirect:/commandes";
    }

    @GetMapping("/delete/{id}")
    public String deleteCommande(@PathVariable Long id) {
        commandeService.deleteCommande(id);
        return "redirect:/commandes";
    }

    @GetMapping("/matieres-premieres")
    public String calculerMatieresPremieres(@RequestParam LocalDate date, Model model) {
        model.addAttribute("matieresNecessaires", commandeService.calculerMatieresPremieresPourDate(date));
        return "commandes/matieres-premieres";
    }

    @GetMapping("/non-traitees")
    public String listCommandesNonTraitees(Model model) {
        // Fetch untreated orders
        List<CommandeDTO> commandes = commandeService.getCommandesNonTraitees();

        if (!commandes.isEmpty()) {
            log.debug("Première commande non traitée - processed: {}", commandes.get(0).isProcessed());
        }
        // Fetch product names for each order
        for (CommandeDTO commande : commandes) {
            Map<Long, String> nomsProduits = new HashMap<>();
            if (commande.getProduitsCommandes() != null) {
                for (Map.Entry<Long, Integer> entry : commande.getProduitsCommandes().entrySet()) {
                    ProduitDTO produit = produitService.getProduitById(entry.getKey());
                    if (produit != null) {
                        nomsProduits.put(entry.getKey(), produit.getNom());
                    }
                }
            }
            commande.setNomsProduits(nomsProduits); // Set the product names in the CommandeDTO
        }

        // Add the list of orders to the model
        model.addAttribute("commandes", commandes);
        return "commandes/non-traitees";
    }

    @GetMapping("/view/{id}")
    public String viewCommande(@PathVariable Long id, Model model) {
        CommandeDTO commandeDTO = commandeService.getCommandeById(id);
        Map<ProduitDTO, Integer> produitsAvecNoms = new HashMap<>();

        // Convertir la Map<Long, Integer> en Map<ProduitDTO, Integer>
        commandeDTO.getProduitsCommandes().forEach((produitId, quantite) -> {
            ProduitDTO produit = produitService.getProduitById(produitId);
            produitsAvecNoms.put(produit, quantite);
        });

        model.addAttribute("commande", commandeDTO);
        model.addAttribute("produitsAvecNoms", produitsAvecNoms);
        return "commandes/view";
    }
}