package lab.hang.Gestion.boulangerie.controller;

import lab.hang.Gestion.boulangerie.model.CompteBancaire;
import lab.hang.Gestion.boulangerie.service.FinanceService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/compte-bancaire")
public class CompteBancaireController {

    private final FinanceService financeService;

    public CompteBancaireController(FinanceService financeService) {
        this.financeService = financeService;
    }

    @GetMapping
    public String getComptesBancaires(Model model) {
        List<CompteBancaire> comptes = financeService.getAllComptesBancaires();
        model.addAttribute("comptes", comptes);
        // Compte principal pour l'affichage rapide du solde
        try {
            model.addAttribute("comptePrincipal", financeService.getCompteBancairePrincipal());
        } catch (Exception e) {
            model.addAttribute("comptePrincipal", null);
        }
        return "finances/compte-bancaire";
    }

    @GetMapping("/creer")
    @PreAuthorize("hasRole('ADMIN')")
    public String showCreerCompteBancaireForm(Model model) {
        model.addAttribute("compteBancaire", new CompteBancaire());
        return "finances/compte";
    }

    @PostMapping("/creer")
    @PreAuthorize("hasRole('ADMIN')")
    public String creerCompteBancaire(@RequestParam String nom,
                                      @RequestParam double soldeInitial,
                                      RedirectAttributes redirectAttributes) {
        financeService.creerCompteBancaire(nom, soldeInitial);
        redirectAttributes.addFlashAttribute("successMessage", "Compte bancaire \"" + nom + "\" créé avec succès.");
        return "redirect:/compte-bancaire";
    }

    @GetMapping("/{id}")
    public String getCompteBancaire(@PathVariable Long id, Model model) {
        CompteBancaire compte = financeService.getCompteBancaire(id);
        model.addAttribute("compte", compte);
        return "finances/compte-bancaire";
    }
}
