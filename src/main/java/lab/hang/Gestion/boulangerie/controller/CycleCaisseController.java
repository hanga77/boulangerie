package lab.hang.Gestion.boulangerie.controller;

import lab.hang.Gestion.boulangerie.application.production.ProductionPersistencePort;
import lab.hang.Gestion.boulangerie.application.sales.CatalogSalesPort;
import lab.hang.Gestion.boulangerie.application.sales.CreateVenteLibreV2UseCase;
import lab.hang.Gestion.boulangerie.application.sales.SalesPersistencePort;
import lab.hang.Gestion.boulangerie.domain.production.ProductionLigneProduit;
import lab.hang.Gestion.boulangerie.domain.production.ProductionStatus;
import lab.hang.Gestion.boulangerie.domain.production.ProductionV2;
import lab.hang.Gestion.boulangerie.infrastructure.persistence.ProductionV2Repository;
import lab.hang.Gestion.boulangerie.model.User;
import lab.hang.Gestion.boulangerie.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Phase 3 — Caisse POS branchée sur le stock restant ProductionV2.
 */
@Controller
@RequestMapping("/cycle/caisse")
@PreAuthorize("hasAnyRole('ADMIN','MANAGER','CAISSIER')")
public class CycleCaisseController {

    private final ProductionV2Repository productionRepo;
    private final ProductionPersistencePort productionPort;
    private final CreateVenteLibreV2UseCase createVente;
    private final SalesPersistencePort salesPort;
    private final CatalogSalesPort catalogPort;
    private final UserService userService;

    public CycleCaisseController(ProductionV2Repository productionRepo,
                                 ProductionPersistencePort productionPort,
                                 CreateVenteLibreV2UseCase createVente,
                                 SalesPersistencePort salesPort,
                                 CatalogSalesPort catalogPort,
                                 UserService userService) {
        this.productionRepo = productionRepo;
        this.productionPort = productionPort;
        this.createVente = createVente;
        this.salesPort = salesPort;
        this.catalogPort = catalogPort;
        this.userService = userService;
    }

    private Optional<ProductionV2> resolveProduction(Long productionId) {
        if (productionId != null) {
            return productionRepo.findByIdWithLignes(productionId);
        }
        LocalDate today = LocalDate.now();
        return productionRepo.findByDateProductionWithLignes(today)
                .or(() -> productionRepo.findByDateProductionWithLignes(today.minusDays(1)));
    }

    private Map<Long, Object[]> restantsView(ProductionV2 p) {
        Map<Long, Object[]> map = new LinkedHashMap<>();
        for (ProductionLigneProduit l : p.getLignesProduits()) {
            if (l.getQuantiteRestante() > 0) {
                map.put(l.getProduitId(), new Object[]{
                        catalogPort.nomProduit(l.getProduitId()),
                        l.getQuantiteRestante(),
                        catalogPort.prixProduit(l.getProduitId())
                });
            }
        }
        return map;
    }

    @GetMapping({"", "/{productionId}"})
    public String caisse(
            @PathVariable(required = false) Long productionId,
            Model model,
            RedirectAttributes ra) {
        Optional<ProductionV2> opt = resolveProduction(productionId);
        if (opt.isEmpty()) {
            ra.addFlashAttribute("warningMessage",
                    "Aucune production V2 validée pour aujourd'hui ou hier.");
            return "redirect:/cycle";
        }
        ProductionV2 p = opt.get();
        if (p.getStatut() != ProductionStatus.VALIDEE && p.getStatut() != ProductionStatus.CLOTUREE) {
            ra.addFlashAttribute("warningMessage",
                    "La production doit être validée avant la caisse (statut: " + p.getStatut() + ")");
            return "redirect:/cycle";
        }
        model.addAttribute("production", p);
        model.addAttribute("restants", restantsView(p));
        model.addAttribute("ventes", salesPort.findVentesByProduction(p.getId()));
        return "cycle/caisse";
    }

    @PostMapping("/{productionId}")
    public String vendre(
            @PathVariable Long productionId,
            @RequestParam(defaultValue = "ESPECES") String moyenPaiement,
            @RequestParam Map<String, String> formData,
            RedirectAttributes ra) {
        try {
            User user = userService.getCurrentUser();
            Map<Long, Integer> qtes = new HashMap<>();
            formData.forEach((k, v) -> {
                if (k.startsWith("qte[") && v != null && !v.isBlank()) {
                    Long pid = Long.valueOf(k.substring(4, k.indexOf(']')));
                    int q = Integer.parseInt(v);
                    if (q > 0) qtes.put(pid, q);
                }
            });
            var saved = createVente.execute(new CreateVenteLibreV2UseCase.Command(
                    productionId, user.getId(), null, moyenPaiement, qtes));
            ra.addFlashAttribute("successMessage",
                    "Vente #" + saved.getId() + " — "
                            + String.format("%,.0f", saved.getMontantTotal()) + " XAF ("
                            + saved.getMoyenPaiement() + ")");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/cycle/caisse/" + productionId;
    }
}
