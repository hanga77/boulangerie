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
 * Phase 6 P3 — saisie des dépenses (trésorerie unifiée).
 */
@Controller
@RequestMapping("/cycle/depenses")
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
public class DepenseController {

    private final FinanceService financeService;
    private final TransactionRepository transactionRepository;

    public DepenseController(FinanceService financeService,
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

        List<Transaction> depenses = transactionRepository.findByDateBetweenAndTypeIn(
                debut, fin, TransactionTypes.EXPENSE_TYPES);

        double total = depenses.stream().mapToDouble(Transaction::getMontant).sum();

        model.addAttribute("debut", debut);
        model.addAttribute("fin", fin);
        model.addAttribute("depenses", depenses);
        model.addAttribute("total", total);
        model.addAttribute("types", TransactionTypes.EXPENSE_TYPES);

        try {
            CompteBancaire c = financeService.getCompteBancairePrincipal();
            model.addAttribute("compteId", c.getId());
            model.addAttribute("solde", c.getSolde());
            model.addAttribute("compteNom", c.getNom());
        } catch (Exception e) {
            model.addAttribute("compteId", null);
            model.addAttribute("solde", null);
        }

        return "cycle/depenses";
    }

    @PostMapping
    public String creer(
            @RequestParam String type,
            @RequestParam double montant,
            @RequestParam String description,
            @RequestParam(required = false) Long compteId,
            RedirectAttributes ra) {

        if (!TransactionTypes.EXPENSE_TYPES.contains(type)) {
            ra.addFlashAttribute("errorMessage", "Type de dépense non autorisé: " + type);
            return "redirect:/cycle/depenses";
        }
        if (montant <= 0) {
            ra.addFlashAttribute("errorMessage", "Le montant doit être positif");
            return "redirect:/cycle/depenses";
        }
        if (description == null || description.isBlank()) {
            ra.addFlashAttribute("errorMessage", "Description obligatoire");
            return "redirect:/cycle/depenses";
        }

        try {
            if (compteId == null) {
                compteId = financeService.getCompteBancairePrincipal().getId();
            }
            financeService.creerTransaction(type, montant, description.trim(), compteId);
            ra.addFlashAttribute("successMessage",
                    "Dépense enregistrée: " + type + " — " + (long) montant + " XAF");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/cycle/depenses";
    }
}
