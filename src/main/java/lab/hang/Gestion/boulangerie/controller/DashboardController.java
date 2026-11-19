package lab.hang.Gestion.boulangerie.controller;

import lab.hang.Gestion.boulangerie.application.finance.UnifiedReportingService;
import lab.hang.Gestion.boulangerie.application.finance.UnifiedReportingService.CaBreakdown;
import lab.hang.Gestion.boulangerie.model.Production;
import lab.hang.Gestion.boulangerie.repository.NotificationRepository;
import lab.hang.Gestion.boulangerie.service.CommandeService;
import lab.hang.Gestion.boulangerie.service.FinanceService;
import lab.hang.Gestion.boulangerie.service.LivraisonService;
import lab.hang.Gestion.boulangerie.service.MatierePremiereService;
import lab.hang.Gestion.boulangerie.service.OperationalReadinessService;
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
    private final UnifiedReportingService unifiedReportingService;
    private final MatierePremiereService matierePremiereService;
    private final ProductionService productionService;
    private final LivraisonService livraisonService;
    private final NotificationRepository notificationRepository;
    private final OperationalReadinessService readinessService;

    public DashboardController(CommandeService commandeService,
                               FinanceService financeService,
                               UnifiedReportingService unifiedReportingService,
                               MatierePremiereService matierePremiereService,
                               ProductionService productionService,
                               LivraisonService livraisonService,
                               NotificationRepository notificationRepository,
                               OperationalReadinessService readinessService) {
        this.commandeService = commandeService;
        this.financeService = financeService;
        this.unifiedReportingService = unifiedReportingService;
        this.matierePremiereService = matierePremiereService;
        this.productionService = productionService;
        this.livraisonService = livraisonService;
        this.notificationRepository = notificationRepository;
        this.readinessService = readinessService;
    }

    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        LocalDate today = LocalDate.now();
        LocalDate debutMois = today.withDayOfMonth(1);

        int nombreCommandesAujourdhui = commandeService.getNombreCommandesPourDate(today);
        double coutTotalAujourdhui = commandeService.getCoutTotalPourDate(today);
        int nombrePointsDeVenteActifs = commandeService.getNombrePointsDeVenteActifsPourDate(today);

        CaBreakdown jour = unifiedReportingService.report(today, today);
        CaBreakdown mois = unifiedReportingService.report(debutMois, today);

        double coutsProduction = financeService.calculerCoutsProduction();
        double revenusVentes = mois.totalCa();
        double profitTotal = mois.resultat();

        List<Production> productionDTOS = productionService.getProductionsByDate(today);

        model.addAttribute("coutsProduction", coutsProduction);
        model.addAttribute("revenusVentes", revenusVentes);
        model.addAttribute("profitTotal", profitTotal);
        model.addAttribute("caJour", jour.totalCa());
        model.addAttribute("caLivraisonV2Jour", jour.livraisonV2());
        model.addAttribute("caCaisseV2Jour", jour.caisseV2());
        model.addAttribute("nombreCommandesAujourdhui", nombreCommandesAujourdhui);
        model.addAttribute("coutTotalAujourdhui", coutTotalAujourdhui);
        model.addAttribute("nombrePointsDeVenteActifs", nombrePointsDeVenteActifs);
        model.addAttribute("commandesRecentes", commandeService.getCommandesByDate(today));
        model.addAttribute("stocks", matierePremiereService.getAllMatierePremieres());
        model.addAttribute("produits", productionDTOS);
        model.addAttribute("livraisons", livraisonService.getLivraisonsByDateRange(today.minusDays(7), today));
        model.addAttribute("notifications", notificationRepository.findTop8ByOrderByDateCreationDesc());
        model.addAttribute("opsAlerts", readinessService.alertsForToday());
        model.addAttribute("hasComptePrincipal", readinessService.hasComptePrincipal());

        return "dashboard";
    }

    @GetMapping("/a-propos")
    public String aPropos() {
        return "a-propos";
    }
}
