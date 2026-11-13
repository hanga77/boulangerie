package lab.hang.Gestion.boulangerie.controller;

import lab.hang.Gestion.boulangerie.application.inventory.ConfirmAllTheoreticalExitsUseCase;
import lab.hang.Gestion.boulangerie.application.inventory.GetMaterialsToSupplyUseCase;
import lab.hang.Gestion.boulangerie.application.production.CloseDailyCycleUseCase;
import lab.hang.Gestion.boulangerie.application.production.DailyCycleSnapshot;
import lab.hang.Gestion.boulangerie.application.production.GetDailyCycleStatusUseCase;
import lab.hang.Gestion.boulangerie.application.production.StartDailyProductionUseCase;
import lab.hang.Gestion.boulangerie.application.production.ValidateProductionQuantitiesUseCase;
import lab.hang.Gestion.boulangerie.domain.production.ProductionV2;
import lab.hang.Gestion.boulangerie.model.User;
import lab.hang.Gestion.boulangerie.repository.MatierePremiereRepository;
import lab.hang.Gestion.boulangerie.repository.ProduitRepository;
import lab.hang.Gestion.boulangerie.service.UserService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Phase 2 — écrans Thymeleaf du cycle journalier V2.
 */
@Controller
@RequestMapping("/cycle")
public class CycleJournalierMvcController {

    private final GetDailyCycleStatusUseCase cycleStatus;
    private final StartDailyProductionUseCase startProduction;
    private final GetMaterialsToSupplyUseCase materialsToSupply;
    private final ConfirmAllTheoreticalExitsUseCase confirmExits;
    private final ValidateProductionQuantitiesUseCase validateProduction;
    private final CloseDailyCycleUseCase closeCycle;
    private final UserService userService;
    private final MatierePremiereRepository matiereRepo;
    private final ProduitRepository produitRepo;

    public CycleJournalierMvcController(GetDailyCycleStatusUseCase cycleStatus,
                                        StartDailyProductionUseCase startProduction,
                                        GetMaterialsToSupplyUseCase materialsToSupply,
                                        ConfirmAllTheoreticalExitsUseCase confirmExits,
                                        ValidateProductionQuantitiesUseCase validateProduction,
                                        CloseDailyCycleUseCase closeCycle,
                                        UserService userService,
                                        MatierePremiereRepository matiereRepo,
                                        ProduitRepository produitRepo) {
        this.cycleStatus = cycleStatus;
        this.startProduction = startProduction;
        this.materialsToSupply = materialsToSupply;
        this.confirmExits = confirmExits;
        this.validateProduction = validateProduction;
        this.closeCycle = closeCycle;
        this.userService = userService;
        this.matiereRepo = matiereRepo;
        this.produitRepo = produitRepo;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public String dashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model) {
        LocalDate jour = date != null ? date : LocalDate.now();
        DailyCycleSnapshot snap = cycleStatus.execute(jour);
        model.addAttribute("snap", snap);
        model.addAttribute("date", jour);
        return "cycle/index";
    }

    @GetMapping("/lancer")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','BOULANGER')")
    public String formLancer(Model model) {
        model.addAttribute("dateActuelle", LocalDate.now());
        model.addAttribute("matieresPremieres", matiereRepo.findAll());
        return "cycle/lancer";
    }

    @PostMapping("/lancer")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','BOULANGER')")
    public String lancer(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long farinageMatiereId,
            @RequestParam(required = false) Double farinageQuantite,
            RedirectAttributes ra) {
        try {
            User user = userService.getCurrentUser();
            ProductionV2 p = startProduction.execute(
                    new StartDailyProductionUseCase.Command(
                            date, user.getId(), farinageMatiereId, farinageQuantite));
            ra.addFlashAttribute("successMessage",
                    "Production #" + p.getId() + " lancée — le magasinier peut fournir le fournil.");
            return "redirect:/cycle/feuille/" + p.getId();
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/cycle/lancer";
        }
    }

    @GetMapping("/feuille/{productionId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','MAGASINIER','BOULANGER')")
    public String feuille(@PathVariable Long productionId, Model model, RedirectAttributes ra) {
        try {
            var result = materialsToSupply.execute(
                    new GetMaterialsToSupplyUseCase.Command(productionId));
            model.addAttribute("result", result);
            return "cycle/feuille-sortie";
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/cycle";
        }
    }

    @PostMapping("/feuille/{productionId}/confirmer")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','MAGASINIER')")
    public String confirmerSorties(
            @PathVariable Long productionId,
            @RequestParam Map<String, String> formData,
            RedirectAttributes ra) {
        try {
            User user = userService.getCurrentUser();
            Map<Long, Double> ajustements = new HashMap<>();
            formData.forEach((key, value) -> {
                if (key.startsWith("qte[") && value != null && !value.isBlank()) {
                    Long matId = Long.valueOf(key.substring(4, key.indexOf(']')));
                    ajustements.put(matId, Double.valueOf(value.replace(',', '.')));
                }
            });
            // Si aucune qte saisie, corps vide → théorique entier
            var cmd = ajustements.isEmpty()
                    ? new ConfirmAllTheoreticalExitsUseCase.Command(productionId, user.getId())
                    : new ConfirmAllTheoreticalExitsUseCase.Command(productionId, user.getId(), ajustements);
            var result = confirmExits.execute(cmd);
            ra.addFlashAttribute("successMessage",
                    result.sorties().size() + " matière(s) sortie(s) du magasin.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/cycle/feuille/" + productionId;
    }

    @GetMapping("/valider/{productionId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','BOULANGER')")
    public String formValider(@PathVariable Long productionId, Model model, RedirectAttributes ra) {
        try {
            DailyCycleSnapshot snap = cycleStatus.execute(LocalDate.now());
            // recharge via materials pour avoir les lignes
            var feuille = materialsToSupply.execute(
                    new GetMaterialsToSupplyUseCase.Command(productionId));
            model.addAttribute("productionId", productionId);
            model.addAttribute("feuille", feuille);
            model.addAttribute("produits", produitRepo.findAll());
            model.addAttribute("snap", snap);
            return "cycle/valider";
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/cycle";
        }
    }

    @PostMapping("/valider/{productionId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','BOULANGER')")
    public String valider(
            @PathVariable Long productionId,
            @RequestParam Map<String, String> formData,
            RedirectAttributes ra) {
        try {
            User user = userService.getCurrentUser();
            Map<Long, Integer> qProduits = new HashMap<>();
            Map<Long, Double> qMatieres = new HashMap<>();
            formData.forEach((key, value) -> {
                if (value == null || value.isBlank()) return;
                if (key.startsWith("produit[")) {
                    Long id = Long.valueOf(key.substring(8, key.indexOf(']')));
                    qProduits.put(id, Integer.valueOf(value));
                } else if (key.startsWith("matiere[")) {
                    Long id = Long.valueOf(key.substring(8, key.indexOf(']')));
                    qMatieres.put(id, Double.valueOf(value.replace(',', '.')));
                }
            });
            var result = validateProduction.execute(
                    new ValidateProductionQuantitiesUseCase.Command(
                            productionId, user.getId(), qProduits, qMatieres));
            String msg = "Production validée.";
            if (result.nbIncidentsDetectes() > 0) {
                msg += " " + result.nbIncidentsDetectes() + " écart(s) détecté(s).";
            }
            ra.addFlashAttribute("successMessage", msg);
            return "redirect:/cycle";
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/cycle/valider/" + productionId;
        }
    }

    @PostMapping("/cloturer")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String cloturer(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "false") boolean forcer,
            RedirectAttributes ra) {
        try {
            User user = userService.getCurrentUser();
            var result = closeCycle.execute(
                    new CloseDailyCycleUseCase.Command(
                            date != null ? date : LocalDate.now(), user.getId(), forcer));
            ra.addFlashAttribute("successMessage", result.message());
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/cycle";
    }
}
