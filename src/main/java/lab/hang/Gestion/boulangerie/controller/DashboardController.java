package lab.hang.Gestion.boulangerie.controller;

import lab.hang.Gestion.boulangerie.model.Production;
import lab.hang.Gestion.boulangerie.repository.NotificationRepository;
import lab.hang.Gestion.boulangerie.service.CommandeService;
import lab.hang.Gestion.boulangerie.service.FinanceService;
import lab.hang.Gestion.boulangerie.service.LivraisonService;
import lab.hang.Gestion.boulangerie.service.MatierePremiereService;
import lab.hang.Gestion.boulangerie.service.ProductionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.List;

@Controller
public class DashboardController {

    private final CommandeService commandeService;
    private final FinanceService financeService;
    private final MatierePremiereService matierePremiereService;
    private final ProductionService productionService;
    private final LivraisonService livraisonService;
    private final NotificationRepository notificationRepository;

    public DashboardController(CommandeService commandeService, FinanceService financeService,
                               MatierePremiereService matierePremiereService,
                               ProductionService productionService,
                               LivraisonService livraisonService,
                               NotificationRepository notificationRepository) {
        this.commandeService = commandeService;
        this.financeService = financeService;
        this.matierePremiereService = matierePremiereService;
        this.productionService = productionService;
        this.livraisonService = livraisonService;
        this.notificationRepository = notificationRepository;
    }

    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        LocalDate today = LocalDate.now();

        int nombreCommandesAujourdhui = commandeService.getNombreCommandesPourDate(today);
        double coutTotalAujourdhui = commandeService.getCoutTotalPourDate(today);
        int nombrePointsDeVenteActifs = commandeService.getNombrePointsDeVenteActifsPourDate(today);

        double coutsProduction = financeService.calculerCoutsProduction();
        double revenusVentes = financeService.calculerRevenusVentes();
        double profitTotal = financeService.calculerProfitTotal();

        List<Production> productionDTOS = productionService.getProductionsByDate(today);

        model.addAttribute("coutsProduction", coutsProduction);
        model.addAttribute("revenusVentes", revenusVentes);
        model.addAttribute("profitTotal", profitTotal);
        model.addAttribute("nombreCommandesAujourdhui", nombreCommandesAujourdhui);
        model.addAttribute("coutTotalAujourdhui", coutTotalAujourdhui);
        model.addAttribute("nombrePointsDeVenteActifs", nombrePointsDeVenteActifs);
        model.addAttribute("commandesRecentes", commandeService.getCommandesByDate(today));
        model.addAttribute("stocks", matierePremiereService.getAllMatierePremieres());
        model.addAttribute("produits", productionDTOS);
        model.addAttribute("livraisons", livraisonService.getLivraisonsByDateRange(today.minusDays(7), today));
        model.addAttribute("notifications", notificationRepository.findTop8ByOrderByDateCreationDesc());

        return "dashboard";
    }

    @GetMapping("/a-propos")
    public String aPropos() {
        return "a-propos";
    }
}