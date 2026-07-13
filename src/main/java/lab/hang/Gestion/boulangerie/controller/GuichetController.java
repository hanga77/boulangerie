package lab.hang.Gestion.boulangerie.controller;

import lab.hang.Gestion.boulangerie.dto.CreateVenteLibreRequest;
import lab.hang.Gestion.boulangerie.model.Guichet;
import lab.hang.Gestion.boulangerie.model.MoyenPaiement;
import lab.hang.Gestion.boulangerie.model.Production;
import lab.hang.Gestion.boulangerie.repository.GuichetRepository;
import lab.hang.Gestion.boulangerie.repository.ProductionRepository;
import lab.hang.Gestion.boulangerie.service.AppSettingsService;
import lab.hang.Gestion.boulangerie.service.VenteLibreService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

@Controller
@RequestMapping("/guichet")
@PreAuthorize("hasRole('CAISSIER')")
public class GuichetController {

    private final GuichetRepository guichetRepository;
    private final ProductionRepository productionRepository;
    private final VenteLibreService venteLibreService;
    private final AppSettingsService appSettingsService;

    public GuichetController(GuichetRepository guichetRepository,
                             ProductionRepository productionRepository,
                             VenteLibreService venteLibreService,
                             AppSettingsService appSettingsService) {
        this.guichetRepository = guichetRepository;
        this.productionRepository = productionRepository;
        this.venteLibreService = venteLibreService;
        this.appSettingsService = appSettingsService;
    }

    @GetMapping
    public String selectGuichet(Model model) {
        List<Guichet> guichets = guichetRepository.findByActifTrue();
        model.addAttribute("guichets", guichets);
        return "guichet/select";
    }

    @GetMapping("/vente")
    @Transactional(readOnly = true)
    public String pos(@RequestParam Long guichetId, Model model, RedirectAttributes ra) {
        Guichet guichet = guichetRepository.findByIdWithPointDeVente(guichetId).orElse(null);
        if (guichet == null) return "redirect:/guichet";

        LocalDate today = LocalDate.now();
        List<Production> productions = productionRepository.findByDateProduction(today);
        Production production = productions.isEmpty()
                ? productionRepository.findByDateProduction(today.minusDays(1)).stream().findFirst().orElse(null)
                : productions.get(0);

        if (production == null) {
            ra.addFlashAttribute("errorMessage",
                    "Aucune production disponible aujourd'hui. Contactez le manager.");
            return "redirect:/guichet";
        }

        model.addAttribute("guichet", guichet);
        model.addAttribute("production", production);
        model.addAttribute("produitsRestants", new HashMap<>(production.getProduitsRestants()));
        model.addAttribute("largeurTicketMm", appSettingsService.getLargeurTicketMm());
        model.addAttribute("moyensPaiement", MoyenPaiement.values());
        return "guichet/pos";
    }

    @PostMapping("/vente")
    public String encaisser(@ModelAttribute CreateVenteLibreRequest request,
                            @RequestParam Long guichetId,
                            RedirectAttributes ra) {
        request.setGuichetId(guichetId);
        boolean aucunProduit = request.getProduits() == null ||
                request.getProduits().values().stream().allMatch(q -> q == null || q <= 0);
        if (aucunProduit) {
            ra.addFlashAttribute("errorMessage", "Sélectionnez au moins un produit.");
            return "redirect:/guichet/vente?guichetId=" + guichetId;
        }
        try {
            venteLibreService.createVenteLibre(request);
            ra.addFlashAttribute("successMessage", "Vente enregistrée avec succès.");
            ra.addFlashAttribute("venteOk", true);
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/guichet/vente?guichetId=" + guichetId;
    }
}
