package lab.hang.Gestion.boulangerie.controller;

import lab.hang.Gestion.boulangerie.application.finance.ProductionMargeService;
import lab.hang.Gestion.boulangerie.application.finance.ProductionMargeService.MargeProduction;
import lab.hang.Gestion.boulangerie.application.production.ProductionPersistencePort;
import lab.hang.Gestion.boulangerie.domain.production.ProductionV2;
import lab.hang.Gestion.boulangerie.service.CreditService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/cycle/marge")
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
public class ProductionMargeController {

    private final ProductionMargeService margeService;
    private final ProductionPersistencePort productionPort;
    private final CreditService creditService;

    public ProductionMargeController(ProductionMargeService margeService,
                                     ProductionPersistencePort productionPort,
                                     CreditService creditService) {
        this.margeService = margeService;
        this.productionPort = productionPort;
        this.creditService = creditService;
    }

    @GetMapping
    public String liste(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model) {
        LocalDate d = date != null ? date : LocalDate.now();
        List<ProductionV2> prods = productionPort.findByDate(d);
        List<MargeProduction> marges = new ArrayList<>();
        for (ProductionV2 p : prods) {
            if (p.getId() != null) {
                marges.add(margeService.calculer(p.getId()));
            }
        }
        model.addAttribute("date", d);
        model.addAttribute("marges", marges);
        model.addAttribute("detteFournisseurs", creditService.getTotalGlobal());
        return "cycle/marge-liste";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        MargeProduction m = margeService.calculer(id);
        model.addAttribute("m", m);
        model.addAttribute("detteFournisseurs", creditService.getTotalGlobal());
        return "cycle/marge-detail";
    }
}
