package lab.hang.Gestion.boulangerie.controller;

import lab.hang.Gestion.boulangerie.application.finance.TransactionTypes;
import lab.hang.Gestion.boulangerie.model.CompteBancaire;
import lab.hang.Gestion.boulangerie.model.Transaction;
import lab.hang.Gestion.boulangerie.repository.TransactionRepository;
import lab.hang.Gestion.boulangerie.service.FinanceService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

/**
 * Phase 6 P4 — saisie manuelle des recettes de vente (hors flux automatique V2).
 */
@Controller
@RequestMapping("/cycle/recettes")
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
public class RecetteController {

    /** Types autorisés en saisie manuelle (évite de forger des types V2 automatiques). */
    private static final List<String> TYPES_SAISIE = List.of(
            TransactionTypes.VENTE,
            TransactionTypes.VENTE_LIBRE,
            TransactionTypes.PAIEMENT_FACTURE
    );

    private final FinanceService financeService;
    private final TransactionRepository transactionRepository;

    public RecetteController(FinanceService financeService,
                             TransactionRepository transactionRepository) {
        this.financeService = financeService;
        this.transactionRepository = transactionRepository;
    }

    @GetMapping
    public String page(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin,
            Model model) {

        if (debut == null) debut = LocalDate.now().withDayOfMonth(1);
        if (fin == null) fin = LocalDate.now();

        List<Transaction> recettes = transactionRepository.findByDateBetweenAndTypeIn(
                debut, fin, TransactionTypes.REVENUE_TYPES);

        double total = recettes.stream().mapToDouble(Transaction::getMontant).sum();

        model.addAttribute("debut", debut);
        model.addAttribute("fin", fin);
        model.addAttribute("recettes", recettes);
        model.addAttribute("total", total);
        model.addAttribute("types", TYPES_SAISIE);

        try {
            CompteBancaire c = financeService.getCompteBancairePrincipal();
            model.addAttribute("compteId", c.getId());
            model.addAttribute("solde", c.getSolde());
            model.addAttribute("compteNom", c.getNom());
        } catch (Exception e) {
            model.addAttribute("compteId", null);
            model.addAttribute("solde", null);
        }

        return "cycle/recettes";
    }

    @PostMapping
    public String creer(
            @RequestParam String type,
            @RequestParam double montant,
            @RequestParam String description,
            @RequestParam(required = false) Long compteId,
            RedirectAttributes ra) {

        if (!TYPES_SAISIE.contains(type)) {
            ra.addFlashAttribute("errorMessage",
                    "Type non autorisé en saisie manuelle. Utilisez le cycle V2 pour livraisons/caisse.");
            return "redirect:/cycle/recettes";
        }
        if (montant <= 0) {
            ra.addFlashAttribute("errorMessage", "Le montant doit être positif");
            return "redirect:/cycle/recettes";
        }
        if (description == null || description.isBlank()) {
            ra.addFlashAttribute("errorMessage", "Description obligatoire");
            return "redirect:/cycle/recettes";
        }

        try {
            if (compteId == null) {
                compteId = financeService.getCompteBancairePrincipal().getId();
            }
            financeService.creerTransaction(type, montant, description.trim(), compteId);
            ra.addFlashAttribute("successMessage",
                    "Recette enregistrée: " + type + " — " + (long) montant + " XAF");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/cycle/recettes";
    }
}
