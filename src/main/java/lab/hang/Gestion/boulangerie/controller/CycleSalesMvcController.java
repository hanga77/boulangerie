package lab.hang.Gestion.boulangerie.controller;

import lab.hang.Gestion.boulangerie.application.production.ProductionPersistencePort;
import lab.hang.Gestion.boulangerie.application.sales.CreateLivraisonV2UseCase;
import lab.hang.Gestion.boulangerie.application.sales.CreateVenteLibreV2UseCase;
import lab.hang.Gestion.boulangerie.application.sales.SalesPersistencePort;
import lab.hang.Gestion.boulangerie.domain.production.ProductionV2;
import lab.hang.Gestion.boulangerie.model.User;
import lab.hang.Gestion.boulangerie.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Livraisons et ventes libres liées à ProductionV2.
 */
@Controller
@RequestMapping("/cycle")
public class CycleSalesMvcController {

    private final ProductionPersistencePort productionPort;
    private final SalesPersistencePort salesPort;
    private final CreateLivraisonV2UseCase createLivraison;
    private final CreateVenteLibreV2UseCase createVente;
    private final UserService userService;

    public CycleSalesMvcController(ProductionPersistencePort productionPort,
                                   SalesPersistencePort salesPort,
                                   CreateLivraisonV2UseCase createLivraison,
                                   CreateVenteLibreV2UseCase createVente,
                                   UserService userService) {
        this.productionPort = productionPort;
        this.salesPort = salesPort;
        this.createLivraison = createLivraison;
        this.createVente = createVente;
        this.userService = userService;
    }

    private Map<Long, Object[]> restantsView(ProductionV2 p) {
        Map<Long, Object[]> map = new LinkedHashMap<>();
        if (p.getLignesProduits() == null) return map;
        p.getLignesProduits().forEach(l -> {
            int rest = l.getQuantiteRestante();
            if (rest > 0 && l.getProduit() != null) {
                map.put(l.getProduit().getId(), new Object[]{
                        l.getProduit().getNom(),
                        rest,
                        l.getProduit().getPrixUnitaire()
                });
            }
        });
        return map;
    }

    @GetMapping("/livraison/{productionId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String formLivraison(@PathVariable Long productionId, Model model, RedirectAttributes ra) {
        try {
            ProductionV2 p = productionPort.getById(productionId);
            model.addAttribute("production", p);
            model.addAttribute("restants", restantsView(p));
            model.addAttribute("livraisons", salesPort.findLivraisonsByProduction(productionId));
            return "cycle/livraison";
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/cycle";
        }
    }

    @PostMapping("/livraison/{productionId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String saveLivraison(
            @PathVariable Long productionId,
            @RequestParam String nomClient,
            @RequestParam(required = false) Long pointDeVenteId,
            @RequestParam(defaultValue = "true") boolean enregistrerRevenu,
            @RequestParam Map<String, String> formData,
            RedirectAttributes ra) {
        try {
            User user = userService.getCurrentUser();
            Map<Long, CreateLivraisonV2UseCase.LigneCmd> lignes = new HashMap<>();
            formData.forEach((k, v) -> {
                if (k.startsWith("qte[") && v != null && !v.isBlank()) {
                    Long pid = Long.valueOf(k.substring(4, k.indexOf(']')));
                    int qte = Integer.parseInt(v);
                    String prixStr = formData.getOrDefault("prix[" + pid + "]", "0");
                    double prix = Double.parseDouble(prixStr.replace(',', '.'));
                    if (qte > 0) lignes.put(pid, new CreateLivraisonV2UseCase.LigneCmd(qte, prix));
                }
            });
            if (lignes.isEmpty()) {
                throw new IllegalArgumentException("Indiquez au moins une quantité produit > 0.");
            }
            var saved = createLivraison.execute(new CreateLivraisonV2UseCase.Command(
                    productionId, nomClient, pointDeVenteId, user.getId(), lignes, enregistrerRevenu));
            ra.addFlashAttribute("successMessage",
                    "Livraison #" + saved.getId() + " créée — "
                            + String.format("%.0f", saved.getMontantTotal()) + " XAF");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/cycle/livraison/" + productionId;
    }

    @GetMapping("/vente/{productionId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','CAISSIER')")
    public String formVente(@PathVariable Long productionId, Model model, RedirectAttributes ra) {
        try {
            ProductionV2 p = productionPort.getById(productionId);
            model.addAttribute("production", p);
            model.addAttribute("restants", restantsView(p));
            model.addAttribute("ventes", salesPort.findVentesByProduction(productionId));
            return "cycle/vente";
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/cycle";
        }
    }

    @PostMapping("/vente/{productionId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','CAISSIER')")
    public String saveVente(
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
            if (qtes.isEmpty()) {
                throw new IllegalArgumentException("Indiquez au moins une quantité produit > 0.");
            }
            var saved = createVente.execute(new CreateVenteLibreV2UseCase.Command(
                    productionId, user.getId(), null, moyenPaiement, qtes));
            ra.addFlashAttribute("successMessage",
                    "Vente #" + saved.getId() + " — "
                            + String.format("%.0f", saved.getMontantTotal()) + " XAF ("
                            + saved.getMoyenPaiement() + ")");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/cycle/vente/" + productionId;
    }
}
