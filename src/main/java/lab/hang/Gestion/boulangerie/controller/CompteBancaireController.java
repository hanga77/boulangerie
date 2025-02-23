package lab.hang.Gestion.boulangerie.controller;

import lab.hang.Gestion.boulangerie.model.CompteBancaire;
import lab.hang.Gestion.boulangerie.service.FinanceService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/compte-bancaire")
public class CompteBancaireController {

    private final FinanceService financeService;

    public CompteBancaireController(FinanceService financeService) {
        this.financeService = financeService;
    }

    @GetMapping
    public String getCompteBancaire(Model model) {
        CompteBancaire compteBancaire = financeService.getCompteBancairePrincipal();
        model.addAttribute("compteBancaire", compteBancaire);
        return "finances/compte-bancaire";
    }

    @GetMapping("/creer")
    public String showCreerCompteBancaireForm() {
        return "finances/compte";
    }

    @PostMapping("/creer")
    public String creerCompteBancaire(@RequestParam String nom, @RequestParam double soldeInitial) {
        financeService.creerCompteBancaire(nom, soldeInitial);
        return "redirect:/compte-bancaire";
    }
}
