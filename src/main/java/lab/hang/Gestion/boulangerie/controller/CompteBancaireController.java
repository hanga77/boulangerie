package lab.hang.Gestion.boulangerie.controller;

import lab.hang.Gestion.boulangerie.model.CompteBancaire;
import lab.hang.Gestion.boulangerie.model.Transaction;
import lab.hang.Gestion.boulangerie.repository.TransactionRepository;
import lab.hang.Gestion.boulangerie.service.FinanceService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

/**
 * Phase 9 — multi-comptes et virements + F3 Compte Principal 1 clic.
 */
@Controller
@RequestMapping("/compte-bancaire")
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
public class CompteBancaireController {

    private static final List<String> TRANSFERT_TYPES =
            List.of("TRANSFERT_DEBIT", "TRANSFERT_CREDIT");

    private final FinanceService financeService;
    private final TransactionRepository transactionRepository;

    public CompteBancaireController(FinanceService financeService,
                                    TransactionRepository transactionRepository) {
        this.financeService = financeService;
        this.transactionRepository = transactionRepository;
    }

    @GetMapping
    public String getComptesBancaires(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin,
            Model model) {

        List<CompteBancaire> comptes = financeService.getAllComptesBancaires();
        model.addAttribute("comptes", comptes);

        LocalDate d0 = debut != null ? debut : LocalDate.now().withDayOfMonth(1);
        LocalDate d1 = fin != null ? fin : LocalDate.now();
        if (d1.isBefore(d0)) {
            LocalDate tmp = d0;
            d0 = d1;
            d1 = tmp;
        }
        model.addAttribute("debut", d0);
        model.addAttribute("fin", d1);

        List<Transaction> virements = transactionRepository.findByDateBetweenAndTypeIn(
                d0, d1, TRANSFERT_TYPES);
        model.addAttribute("virements", virements);

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
        redirectAttributes.addFlashAttribute("successMessage",
                "Compte bancaire \"" + nom + "\" créé avec succès.");
        return "redirect:/compte-bancaire";
    }

    /**
     * F3 — crée le Compte Principal en un clic si absent (ADMIN).
     */
    @PostMapping("/assurer-principal")
    @PreAuthorize("hasRole('ADMIN')")
    public String assurerPrincipal(
            @RequestParam(defaultValue = "0") double soldeInitial,
            @RequestParam(required = false, defaultValue = "/compte-bancaire") String redirect,
            RedirectAttributes ra) {
        try {
            boolean created = financeService.assurerComptePrincipal(soldeInitial);
            if (created) {
                ra.addFlashAttribute("successMessage",
                        "Compte Principal créé (solde initial : " + (long) soldeInitial + " XAF).");
            } else {
                ra.addFlashAttribute("successMessage", "Compte Principal déjà présent.");
            }
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        if (redirect == null || redirect.isBlank() || !redirect.startsWith("/")) {
            redirect = "/compte-bancaire";
        }
        return "redirect:" + redirect;
    }

    @PostMapping("/transfert")
    public String transfert(@RequestParam Long sourceId,
                            @RequestParam Long destinationId,
                            @RequestParam double montant,
                            @RequestParam(required = false) String description,
                            RedirectAttributes ra) {
        try {
            if (sourceId == null || destinationId == null) {
                throw new IllegalArgumentException("Comptes source et destination obligatoires");
            }
            if (sourceId.equals(destinationId)) {
                throw new IllegalArgumentException("Les comptes source et destination doivent être différents");
            }
            if (montant <= 0) {
                throw new IllegalArgumentException("Le montant doit être positif");
            }
            String desc = (description != null && !description.isBlank())
                    ? description.trim() : "Virement interne";
            financeService.transfertEntreCcomptes(sourceId, destinationId, montant, desc);
            ra.addFlashAttribute("successMessage",
                    "Virement de " + (long) montant + " XAF effectué.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/compte-bancaire";
    }

    @GetMapping("/{id}")
    public String getCompteBancaire(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin,
            Model model) {

        CompteBancaire compte = financeService.getCompteBancaire(id);
        model.addAttribute("compte", compte);
        model.addAttribute("comptes", financeService.getAllComptesBancaires());

        LocalDate d0 = debut != null ? debut : LocalDate.now().withDayOfMonth(1);
        LocalDate d1 = fin != null ? fin : LocalDate.now();
        model.addAttribute("debut", d0);
        model.addAttribute("fin", d1);

        List<Transaction> mouvements = transactionRepository.findByDateBetween(d0, d1).stream()
                .filter(t -> t.getCompteBancaire() != null
                        && t.getCompteBancaire().getId() != null
                        && t.getCompteBancaire().getId().equals(id))
                .toList();
        model.addAttribute("mouvements", mouvements);
        return "finances/compte-bancaire";
    }
}
