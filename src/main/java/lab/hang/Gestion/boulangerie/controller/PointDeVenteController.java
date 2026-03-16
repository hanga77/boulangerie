package lab.hang.Gestion.boulangerie.controller;

import jakarta.validation.Valid;
import lab.hang.Gestion.boulangerie.dto.GuichetDTO;
import lab.hang.Gestion.boulangerie.dto.PointDeVenteDTO;
import lab.hang.Gestion.boulangerie.model.TypePointDeVente;
import lab.hang.Gestion.boulangerie.service.PointDeVenteService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/points-de-vente")
public class PointDeVenteController {

    private final PointDeVenteService pointDeVenteService;

    public PointDeVenteController(PointDeVenteService pointDeVenteService) {
        this.pointDeVenteService = pointDeVenteService;
    }

    // ─── Liste ────────────────────────────────────────────────────────────────

    @GetMapping
    public String liste(Model model) {
        model.addAttribute("pointsDeVente", pointDeVenteService.getAllPointsDeVente());
        model.addAttribute("newPdv", new PointDeVenteDTO());
        model.addAttribute("types", TypePointDeVente.values());
        return "points-de-vente/list";
    }

    // ─── Créer ────────────────────────────────────────────────────────────────

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("pdv", new PointDeVenteDTO());
        model.addAttribute("types", TypePointDeVente.values());
        model.addAttribute("titre", "Nouveau Point de Vente");
        return "points-de-vente/form";
    }

    @PostMapping
    public String creer(@ModelAttribute("pdv") @Valid PointDeVenteDTO dto,
                        BindingResult result,
                        Model model,
                        RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("types", TypePointDeVente.values());
            model.addAttribute("titre", "Nouveau Point de Vente");
            return "points-de-vente/form";
        }
        pointDeVenteService.creer(dto);
        redirectAttributes.addFlashAttribute("success", "Point de vente créé avec succès");
        return "redirect:/points-de-vente";
    }

    // ─── Modifier ─────────────────────────────────────────────────────────────

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("pdv", pointDeVenteService.getById(id));
        model.addAttribute("types", TypePointDeVente.values());
        model.addAttribute("titre", "Modifier le Point de Vente");
        return "points-de-vente/form";
    }

    @PostMapping("/update/{id}")
    public String modifier(@PathVariable Long id,
                           @ModelAttribute("pdv") @Valid PointDeVenteDTO dto,
                           BindingResult result,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("types", TypePointDeVente.values());
            model.addAttribute("titre", "Modifier le Point de Vente");
            return "points-de-vente/form";
        }
        pointDeVenteService.modifier(id, dto);
        redirectAttributes.addFlashAttribute("success", "Point de vente modifié avec succès");
        return "redirect:/points-de-vente";
    }

    // ─── Détails + stats ──────────────────────────────────────────────────────

    @GetMapping("/{id}")
    public String details(@PathVariable Long id, Model model) {
        model.addAttribute("pdv", pointDeVenteService.getStatsPointDeVente(id));
        model.addAttribute("newGuichet", new GuichetDTO());
        return "points-de-vente/details";
    }

    // ─── Désactiver / Supprimer ───────────────────────────────────────────────

    @PostMapping("/{id}/desactiver")
    @PreAuthorize("hasRole('ADMIN')")
    public String desactiver(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        pointDeVenteService.desactiver(id);
        redirectAttributes.addFlashAttribute("success", "Point de vente désactivé");
        return "redirect:/points-de-vente";
    }

    @PostMapping("/{id}/supprimer")
    @PreAuthorize("hasRole('ADMIN')")
    public String supprimer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            pointDeVenteService.supprimer(id);
            redirectAttributes.addFlashAttribute("success", "Point de vente supprimé");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Impossible de supprimer : " + e.getMessage());
        }
        return "redirect:/points-de-vente";
    }

    // ─── Guichets ─────────────────────────────────────────────────────────────

    @PostMapping("/{pdvId}/guichets")
    public String creerGuichet(@PathVariable Long pdvId,
                               @ModelAttribute("newGuichet") @Valid GuichetDTO dto,
                               BindingResult result,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Données du guichet invalides");
            return "redirect:/points-de-vente/" + pdvId;
        }
        dto.setPointDeVenteId(pdvId);
        pointDeVenteService.creerGuichet(dto);
        redirectAttributes.addFlashAttribute("success", "Guichet créé avec succès");
        return "redirect:/points-de-vente/" + pdvId;
    }

    @PostMapping("/guichets/{guichetId}/supprimer")
    @PreAuthorize("hasRole('ADMIN')")
    public String supprimerGuichet(@PathVariable Long guichetId,
                                   @RequestParam Long pdvId,
                                   RedirectAttributes redirectAttributes) {
        try {
            pointDeVenteService.supprimerGuichet(guichetId);
            redirectAttributes.addFlashAttribute("success", "Guichet supprimé");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Impossible de supprimer : " + e.getMessage());
        }
        return "redirect:/points-de-vente/" + pdvId;
    }
}
