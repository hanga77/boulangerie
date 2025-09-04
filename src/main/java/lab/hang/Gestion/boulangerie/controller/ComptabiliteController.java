package lab.hang.Gestion.boulangerie.controller;

import jakarta.validation.Valid;
import lab.hang.Gestion.boulangerie.dto.ChargeFixeDTO;
import lab.hang.Gestion.boulangerie.dto.FactureDTO;
import lab.hang.Gestion.boulangerie.service.AlerteService;
import lab.hang.Gestion.boulangerie.service.ChargeFixeService;
import lab.hang.Gestion.boulangerie.service.FacturationService;
import lab.hang.Gestion.boulangerie.service.KPIService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/comptabilite")
public class ComptabiliteController {
    private final ChargeFixeService chargeFixeService;
    private final FacturationService facturationService;
    private final KPIService kpiService;
    private final AlerteService alerteService;

    public ComptabiliteController(ChargeFixeService chargeFixeService,
                                  FacturationService facturationService,
                                  KPIService kpiService,
                                  AlerteService alerteService) {
        this.chargeFixeService = chargeFixeService;
        this.facturationService = facturationService;
        this.kpiService = kpiService;
        this.alerteService = alerteService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Charges fixes
        model.addAttribute("chargesAVenir", chargeFixeService.getChargesFixesAVenir(30));
        model.addAttribute("bilanCharges", chargeFixeService.getBilanChargesFixes());

        // Facturation
        model.addAttribute("statsFacturation", facturationService.getStatistiquesFacturation());
        model.addAttribute("facturesImpayees", facturationService.getFacturesImpayees());

        // KPIs
        model.addAttribute("kpisJournaliers", kpiService.getKPIsJournaliers());

        // Alertes
        model.addAttribute("alertesNonVues", alerteService.getAlertesNonVues());

        return "comptabilite/dashboard";
    }

    @GetMapping("/charges-fixes")
    public String chargesFixesListe(Model model) {
        model.addAttribute("charges", chargeFixeService.getAllChargesFixe());
        model.addAttribute("newCharge", new ChargeFixeDTO());
        return "comptabilite/charges-fixes";
    }

    @PostMapping("/charges-fixes")
    public String ajouterChargeFix(@ModelAttribute("newCharge") @Valid ChargeFixeDTO chargeFixeDTO,
                                   BindingResult result,
                                   Model model) {
        if (result.hasErrors()) {
            model.addAttribute("charges", chargeFixeService.getAllChargesFixe());
            return "comptabilite/charges-fixes";
        }

        chargeFixeService.creerChargeFixe(chargeFixeDTO);
        return "redirect:/comptabilite/charges-fixes";
    }

    @GetMapping("/factures")
    public String facturesListe(Model model) {
        model.addAttribute("factures", facturationService.getAllFactures());
        model.addAttribute("newFacture", new FactureDTO());
        return "comptabilite/factures";
    }

    @PostMapping("/factures")
    public String creerFacture(@ModelAttribute("newFacture") @Valid FactureDTO factureDTO,
                               BindingResult result,
                               Model model) {
        if (result.hasErrors()) {
            model.addAttribute("factures", facturationService.getAllFactures());
            return "comptabilite/factures";
        }

        facturationService.creerFacture(factureDTO);
        return "redirect:/comptabilite/factures";
    }

    @GetMapping("/kpis")
    public String kpisDashboard(Model model) {
        model.addAttribute("kpisParCategorie", kpiService.getKPIsGroupedByCategorie());
        return "comptabilite/kpis";
    }

    @GetMapping("/alertes")
    public String alertesListe(Model model) {
        model.addAttribute("alertes", alerteService.getAllAlertes());
        return "comptabilite/alertes";
    }
}
