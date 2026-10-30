package lab.hang.Gestion.boulangerie.controller;

import lab.hang.Gestion.boulangerie.application.finance.TransactionTypes;
import lab.hang.Gestion.boulangerie.application.finance.UnifiedReportingService;
import lab.hang.Gestion.boulangerie.application.finance.UnifiedReportingService.CaBreakdown;
import lab.hang.Gestion.boulangerie.model.Transaction;
import lab.hang.Gestion.boulangerie.repository.TransactionRepository;
import lab.hang.Gestion.boulangerie.service.FinanceService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Phase 8 — reporting avancé : écran + exports CSV.
 */
@Controller
@RequestMapping("/cycle/reporting")
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
public class ReportingExportController {

    private final UnifiedReportingService reportingService;
    private final TransactionRepository transactionRepository;
    private final FinanceService financeService;

    public ReportingExportController(UnifiedReportingService reportingService,
                                     TransactionRepository transactionRepository,
                                     FinanceService financeService) {
        this.reportingService = reportingService;
        this.transactionRepository = transactionRepository;
        this.financeService = financeService;
    }

    @GetMapping
    public String page(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin,
            Model model) {

        CaBreakdown report = reportingService.report(debut, fin);
        List<Transaction> depenses = transactionRepository.findByDateBetweenAndTypeIn(
                report.debut(), report.fin(), TransactionTypes.EXPENSE_TYPES);

        model.addAttribute("report", report);
        model.addAttribute("parSource", reportingService.caParSource(report));
        model.addAttribute("debut", report.debut());
        model.addAttribute("fin", report.fin());
        model.addAttribute("depenses", depenses);
        try {
            model.addAttribute("soldePrincipal", financeService.getCompteBancairePrincipal().getSolde());
        } catch (Exception e) {
            model.addAttribute("soldePrincipal", null);
        }
        return "cycle/reporting";
    }

    /** Export complet : synthèse + toutes les transactions. */
    @GetMapping(value = "/export.csv", produces = "text/csv")
    public ResponseEntity<byte[]> exportCsv(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {

        CaBreakdown b = reportingService.report(debut, fin);
        List<Transaction> all = transactionRepository.findByDateBetween(b.debut(), b.fin());

        StringBuilder sb = new StringBuilder();
        sb.append("sep=;\n");
        appendSynthese(sb, b);
        sb.append("\n# DETAIL_MOUVEMENTS\n");
        sb.append("date;type;description;montant\n");
        for (Transaction t : all) {
            appendLigne(sb, t);
        }

        return csvResponse(sb, "reporting_" + b.debut() + "_" + b.fin() + ".csv");
    }

    /** Export synthèse uniquement (CA par source + totaux). */
    @GetMapping(value = "/synthese.csv", produces = "text/csv")
    public ResponseEntity<byte[]> exportSynthese(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {

        CaBreakdown b = reportingService.report(debut, fin);
        StringBuilder sb = new StringBuilder();
        sb.append("sep=;\n");
        appendSynthese(sb, b);
        sb.append("\nsource;montant\n");
        for (Map.Entry<String, Double> e : reportingService.caParSource(b).entrySet()) {
            sb.append(csv(e.getKey())).append(';').append(fmt(e.getValue())).append('\n');
        }
        return csvResponse(sb, "synthese_" + b.debut() + "_" + b.fin() + ".csv");
    }

    /** Export dépenses seules. */
    @GetMapping(value = "/depenses.csv", produces = "text/csv")
    public ResponseEntity<byte[]> exportDepenses(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {

        CaBreakdown b = reportingService.report(debut, fin);
        List<Transaction> depenses = transactionRepository.findByDateBetweenAndTypeIn(
                b.debut(), b.fin(), TransactionTypes.EXPENSE_TYPES);

        StringBuilder sb = new StringBuilder();
        sb.append("sep=;\n");
        sb.append("periode_debut;").append(b.debut()).append('\n');
        sb.append("periode_fin;").append(b.fin()).append('\n');
        sb.append("total_depenses;").append(fmt(b.depenses())).append('\n');
        sb.append('\n');
        sb.append("date;type;description;montant\n");
        for (Transaction t : depenses) {
            appendLigne(sb, t);
        }
        return csvResponse(sb, "depenses_" + b.debut() + "_" + b.fin() + ".csv");
    }

    private void appendSynthese(StringBuilder sb, CaBreakdown b) {
        sb.append("# SYNTHESE\n");
        sb.append("periode_debut;").append(b.debut()).append('\n');
        sb.append("periode_fin;").append(b.fin()).append('\n');
        sb.append("ca_vente_v1;").append(fmt(b.venteV1())).append('\n');
        sb.append("ca_vente_libre;").append(fmt(b.venteLibreAncien())).append('\n');
        sb.append("ca_livraison_v2;").append(fmt(b.livraisonV2())).append('\n');
        sb.append("ca_caisse_v2;").append(fmt(b.caisseV2())).append('\n');
        sb.append("ca_paiements_facture;").append(fmt(b.paiementsFacture())).append('\n');
        sb.append("total_ca;").append(fmt(b.totalCa())).append('\n');
        sb.append("total_depenses;").append(fmt(b.depenses())).append('\n');
        sb.append("resultat;").append(fmt(b.resultat())).append('\n');
    }

    private void appendLigne(StringBuilder sb, Transaction t) {
        sb.append(t.getDate() != null ? t.getDate() : "").append(';')
                .append(csv(t.getType())).append(';')
                .append(csv(t.getDescription())).append(';')
                .append(fmt(t.getMontant())).append('\n');
    }

    private static ResponseEntity<byte[]> csvResponse(StringBuilder sb, String filename) {
        byte[] body = sb.toString().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(body);
    }

    private static String fmt(double v) {
        return String.format(java.util.Locale.US, "%.2f", v);
    }

    private static String csv(String s) {
        if (s == null) return "";
        String t = s.replace('"', '\'').replace(';', ',');
        if (t.contains("\n") || t.contains(",")) {
            return "\"" + t + "\"";
        }
        return t;
    }
}
