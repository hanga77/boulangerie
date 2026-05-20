package lab.hang.Gestion.boulangerie.controller;

import lab.hang.Gestion.boulangerie.model.Fournisseur;
import lab.hang.Gestion.boulangerie.model.CreditReport;
import lab.hang.Gestion.boulangerie.service.CreditService;
import lab.hang.Gestion.boulangerie.service.FournisseurService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/fournisseurs")
public class FournisseurViewController {

    private final FournisseurService fournisseurService;
    private final CreditService creditService;

    public FournisseurViewController(FournisseurService fournisseurService,
                                     CreditService creditService) {
        this.fournisseurService = fournisseurService;
        this.creditService = creditService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("fournisseurs", fournisseurService.getAllFournisseurs());
        return "fournisseurs/list";
    }

    @GetMapping("/dettes")
    public String listDettes(Model model) {
        model.addAttribute("dettes", creditService.getAllDettesEnCours());
        model.addAttribute("totalGlobal", creditService.getTotalGlobal());
        return "fournisseurs/dettes";
    }

    @PostMapping("/dettes/{detteId}/rembourser")
    public String rembourser(@PathVariable Long detteId,
                             @RequestParam double montant,
                             @RequestParam(required = false) Long fournisseurId,
                             RedirectAttributes ra) {
        creditService.effectuerRemboursement(detteId, montant);
        ra.addFlashAttribute("successMessage", "Remboursement de " + montant + " XAF enregistré.");
        if (fournisseurId != null) {
            return "redirect:/fournisseurs/" + fournisseurId;
        }
        return "redirect:/fournisseurs/dettes";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Fournisseur fournisseur = fournisseurService.getFournisseurById(id);
        CreditReport rapport = creditService.genererRapportCredit(fournisseur);
        model.addAttribute("fournisseur", fournisseur);
        model.addAttribute("rapport", rapport);
        return "fournisseurs/detail";
    }

    @PostMapping("/{id}/dette")
    public String enregistrerDette(@PathVariable Long id,
                                   @RequestParam double montant,
                                   RedirectAttributes ra) {
        Fournisseur fournisseur = fournisseurService.getFournisseurById(id);
        creditService.enregistrerDette(fournisseur, montant);
        ra.addFlashAttribute("successMessage", "Dette de " + montant + " XAF enregistrée.");
        return "redirect:/fournisseurs/" + id;
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
