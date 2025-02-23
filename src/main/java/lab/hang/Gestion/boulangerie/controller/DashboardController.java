package lab.hang.Gestion.boulangerie.controller;

import lab.hang.Gestion.boulangerie.dto.ProductionDTO;
import lab.hang.Gestion.boulangerie.model.Production;
import lab.hang.Gestion.boulangerie.model.Produit;
import lab.hang.Gestion.boulangerie.service.CommandeService;
import lab.hang.Gestion.boulangerie.service.FinanceService;
import lab.hang.Gestion.boulangerie.service.MatierePremiereService;
import lab.hang.Gestion.boulangerie.service.ProductionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

@Controller
public class DashboardController {

    private final CommandeService commandeService;
    private  final FinanceService financeService;
    private  final MatierePremiereService matierePremiereService;

    private  final ProductionService productionService;


    public DashboardController(CommandeService commandeService, FinanceService financeService, MatierePremiereService matierePremiereService, ProductionService productionService) {
        this.commandeService = commandeService;
        this.financeService = financeService;
        this.matierePremiereService = matierePremiereService;
        this.productionService = productionService;
    }

    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        LocalDate today = LocalDate.now();
        System.out.println("Date du jour : " + today);

        int nombreCommandesAujourdhui = commandeService.getNombreCommandesPourDate(today);
        System.out.println("Nombre de commandes aujourd'hui : " + nombreCommandesAujourdhui);

        double coutTotalAujourdhui = commandeService.getCoutTotalPourDate(today);
        System.out.println("Coût total aujourd'hui : " + coutTotalAujourdhui);

        int nombrePointsDeVenteActifs = commandeService.getNombrePointsDeVenteActifsPourDate(today);
        System.out.println("Nombre de points de vente actifs : " + nombrePointsDeVenteActifs);

        double coutsProduction = financeService.calculerCoutsProduction();
        System.out.println("Coûts de production : " + coutsProduction);

        double revenusVentes = financeService.calculerRevenusVentes();
        System.out.println("Revenus des ventes : " + revenusVentes);

        double profitTotal = financeService.calculerProfitTotal();
        System.out.println("Profit total : " + profitTotal);

        List<Production> productionDTOS = productionService.getProductionsByDate(today);
        HashMap<Produit, Double> coutsProduits = new HashMap<>();

        productionDTOS.forEach(Production::getProduitsRestants);
       /* for(Production prod : productionDTOS){
            prod.getProduitsRestants();
        }*/
        model.addAttribute("coutsProduction", coutsProduction);
        model.addAttribute("revenusVentes", revenusVentes);
        model.addAttribute("profitTotal", profitTotal);
        model.addAttribute("nombreCommandesAujourdhui", nombreCommandesAujourdhui);
        model.addAttribute("coutTotalAujourdhui", coutTotalAujourdhui);
        model.addAttribute("nombrePointsDeVenteActifs", nombrePointsDeVenteActifs);
        model.addAttribute("commandesRecentes", commandeService.getCommandesByDate(today));
        model.addAttribute("stocks",matierePremiereService.getAllMatierePremieres());
        model.addAttribute("produits", productionDTOS);

        return "dashboard";
    }


}