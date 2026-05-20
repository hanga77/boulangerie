package lab.hang.Gestion.boulangerie.controller;

import lab.hang.Gestion.boulangerie.model.Fournisseur;
import lab.hang.Gestion.boulangerie.service.FournisseurService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/fournisseurs")
public class FournisseurViewController {

    private final FournisseurService fournisseurService;

    public FournisseurViewController(FournisseurService fournisseurService) {
        this.fournisseurService = fournisseurService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("fournisseurs", fournisseurService.getAllFournisseurs());
        return "fournisseurs/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("fournisseur", new Fournisseur());
        return "fournisseurs/form";
    }

    @PostMapping
    public String create(@ModelAttribute Fournisseur fournisseur, RedirectAttributes ra) {
        fournisseurService.saveFournisseur(fournisseur);
        ra.addFlashAttribute("successMessage", "Fournisseur créé avec succès.");
        return "redirect:/fournisseurs";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("fournisseur", fournisseurService.getFournisseurById(id));
        return "fournisseurs/form";
    }

    @PostMapping("/update/{id}")
    public String update(@PathVariable Long id, @ModelAttribute Fournisseur fournisseur,
                         RedirectAttributes ra) {
        fournisseurService.updateFournisseur(id, fournisseur);
        ra.addFlashAttribute("successMessage", "Fournisseur mis à jour.");
        return "redirect:/fournisseurs";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        fournisseurService.deleteFournisseur(id);
        ra.addFlashAttribute("successMessage", "Fournisseur supprimé.");
        return "redirect:/fournisseurs";
    }
}
