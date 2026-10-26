package lab.hang.Gestion.boulangerie.controller;

import lab.hang.Gestion.boulangerie.application.finance.UnifiedReportingService;
import lab.hang.Gestion.boulangerie.application.finance.UnifiedReportingService.CaBreakdown;
import lab.hang.Gestion.boulangerie.service.FinanceService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

/**
 * Phase 6 P1 — écran CA unifié V1 + V2.
 */
@Controller
@RequestMapping("/cycle/tresorerie")
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
public class TresorerieUnifieeController {

    private final UnifiedReportingService reportingService;
    private final FinanceService financeService;

    public TresorerieUnifieeController(UnifiedReportingService reportingService,
                                       FinanceService financeService) {
        this.reportingService = reportingService;
        this.financeService = financeService;
    }

    @GetMapping
    public String page(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin,
            Model model) {

        CaBreakdown report = reportingService.report(debut, fin);

        model.addAttribute("report", report);
        model.addAttribute("parSource", reportingService.caParSource(report));
        model.addAttribute("debut", report.debut());
        model.addAttribute("fin", report.fin());
        try {
            model.addAttribute("soldePrincipal", financeService.getCompteBancairePrincipal().getSolde());
        } catch (Exception e) {
            model.addAttribute("soldePrincipal", null);
        }
        return "cycle/tresorerie";
    }
}
