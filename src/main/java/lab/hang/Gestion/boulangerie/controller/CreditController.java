package lab.hang.Gestion.boulangerie.controller;

import lab.hang.Gestion.boulangerie.model.CreditReport;
import lab.hang.Gestion.boulangerie.model.FournisseurDette;
import lab.hang.Gestion.boulangerie.service.CreditService;
import lab.hang.Gestion.boulangerie.service.FournisseurService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/credits")
public class CreditController {
    private final CreditService creditService;
    private final FournisseurService fournisseurService;

    public CreditController(CreditService creditService, FournisseurService fournisseurService) {
        this.creditService = creditService;
        this.fournisseurService = fournisseurService;
    }

    @GetMapping("/fournisseur/{fournisseurId}/rapport")
    public ResponseEntity<CreditReport> getRapportCredit(@PathVariable Long fournisseurId) {
        var fournisseur = fournisseurService.getFournisseurById(fournisseurId);
        return ResponseEntity.ok(creditService.genererRapportCredit(fournisseur));
    }

    @PostMapping("/dette/{detteId}/remboursement")
    public ResponseEntity<Void> effectuerRemboursement(
            @PathVariable Long detteId,
            @RequestParam double montant) {
        creditService.effectuerRemboursement(detteId, montant);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/fournisseur/{fournisseurId}/dette-totale")
    public ResponseEntity<Double> getDetteTotale(@PathVariable Long fournisseurId) {
        var fournisseur = fournisseurService.getFournisseurById(fournisseurId);
        return ResponseEntity.ok(creditService.calculerDetteTotale(fournisseur));
    }
}