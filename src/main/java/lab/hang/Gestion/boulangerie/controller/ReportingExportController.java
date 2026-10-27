package lab.hang.Gestion.boulangerie.controller;

import lab.hang.Gestion.boulangerie.application.finance.UnifiedReportingService;
import lab.hang.Gestion.boulangerie.application.finance.UnifiedReportingService.CaBreakdown;
import lab.hang.Gestion.boulangerie.model.Transaction;
import lab.hang.Gestion.boulangerie.repository.TransactionRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

/**
 * Phase 8 — export CSV du reporting unifié.
 */
@Controller
@RequestMapping("/cycle/reporting")
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
public class ReportingExportController {

    private final UnifiedReportingService reportingService;
    private final TransactionRepository transactionRepository;

    public ReportingExportController(UnifiedReportingService reportingService,
                                     TransactionRepository transactionRepository) {
        this.reportingService = reportingService;
        this.transactionRepository = transactionRepository;
    }

    @GetMapping(value = "/export.csv", produces = "text/csv")
    public ResponseEntity<byte[]> exportCsv(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {

        CaBreakdown b = reportingService.report(debut, fin);
        List<Transaction> all = transactionRepository.findByDateBetween(b.debut(), b.fin());

        StringBuilder sb = new StringBuilder();
        sb.append("sep=;\n");
        sb.append("periode_debut;").append(b.debut()).append('\n');
        sb.append("periode_fin;").append(b.fin()).append('\n');
        sb.append("total_ca;").append(fmt(b.totalCa())).append('\n');
        sb.append("total_depenses;").append(fmt(b.depenses())).append('\n');
        sb.append("resultat;").append(fmt(b.resultat())).append('\n');
        sb.append('\n');
        sb.append("date;type;description;montant\n");
        for (Transaction t : all) {
            sb.append(t.getDate() != null ? t.getDate() : "").append(';')
                    .append(csv(t.getType())).append(';')
                    .append(csv(t.getDescription())).append(';')
                    .append(fmt(t.getMontant())).append('\n');
        }

        byte[] body = sb.toString().getBytes(StandardCharsets.UTF_8);
        String filename = "reporting_" + b.debut() + "_" + b.fin() + ".csv";
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
