package lab.hang.Gestion.boulangerie.controller;

import lab.hang.Gestion.boulangerie.application.inventory.ConfirmAllTheoreticalExitsUseCase;
import lab.hang.Gestion.boulangerie.application.inventory.GetMaterialsToSupplyUseCase;
import lab.hang.Gestion.boulangerie.application.production.GetDailyCycleStatusUseCase;
import lab.hang.Gestion.boulangerie.application.production.StartDailyProductionUseCase;
import lab.hang.Gestion.boulangerie.application.production.ValidateProductionQuantitiesUseCase;
import lab.hang.Gestion.boulangerie.domain.production.ProductionV2;
import lab.hang.Gestion.boulangerie.model.User;
import lab.hang.Gestion.boulangerie.service.UserService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

/**
 * API minimale du cycle journalier V2 :
 * lancer production (avec farinage) → feuille magasinier → confirmer sorties.
 */
@RestController
@RequestMapping("/api/cycle")
public class CycleJournalierController {

    private final StartDailyProductionUseCase startProduction;
    private final GetMaterialsToSupplyUseCase materialsToSupply;
    private final ConfirmAllTheoreticalExitsUseCase confirmExits;
    private final ValidateProductionQuantitiesUseCase validateProduction;
    private final GetDailyCycleStatusUseCase cycleStatus;
    private final UserService userService;

    public CycleJournalierController(StartDailyProductionUseCase startProduction,
                                     GetMaterialsToSupplyUseCase materialsToSupply,
                                     ConfirmAllTheoreticalExitsUseCase confirmExits,
                                     ValidateProductionQuantitiesUseCase validateProduction,
                                     GetDailyCycleStatusUseCase cycleStatus,
                                     UserService userService) {
        this.startProduction = startProduction;
        this.materialsToSupply = materialsToSupply;
        this.confirmExits = confirmExits;
        this.validateProduction = validateProduction;
        this.cycleStatus = cycleStatus;
        this.userService = userService;
    }

    public record StartRequest(
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Long farinageMatiereId,
            Double farinageQuantite
    ) {}

    /** Boulanger : lance la production du jour (théorique = commandes + farinage). */
    @PostMapping("/production/start")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','BOULANGER')")
    public ResponseEntity<?> start(@RequestBody(required = false) StartRequest body) {
        User user = userService.getCurrentUser();
        LocalDate date = body != null && body.date() != null ? body.date() : LocalDate.now();
        Long farineId = body != null ? body.farinageMatiereId() : null;
        Double farineQte = body != null ? body.farinageQuantite() : null;

        ProductionV2 p = startProduction.execute(
                new StartDailyProductionUseCase.Command(date, user.getId(), farineId, farineQte));
        return ResponseEntity.ok(Map.of(
                "productionId", p.getId(),
                "date", p.getDateProduction().toString(),
                "statut", p.getStatut().name(),
                "nbLignesMatiere", p.getLignesMatieres().size(),
                "nbLignesProduit", p.getLignesProduits().size(),
                "message", "Production lancée — le magasinier peut consulter la feuille de sortie"
        ));
    }

    /** Magasinier : feuille de sortie (théorique dont farinage). */
    @GetMapping("/production/{id}/matieres-a-fournir")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','MAGASINIER')")
    public ResponseEntity<?> feuilleSortie(@PathVariable Long id) {
        return ResponseEntity.ok(materialsToSupply.execute(
                new GetMaterialsToSupplyUseCase.Command(id)));
    }

    /** Magasinier : confirme toutes les sorties au théorique (ajustements optionnels). */
    @PostMapping("/production/{id}/confirmer-sorties")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','MAGASINIER')")
    public ResponseEntity<?> confirmerSorties(
            @PathVariable Long id,
            @RequestBody(required = false) Map<Long, Double> ajustements) {
        User user = userService.getCurrentUser();
        var result = confirmExits.execute(
                new ConfirmAllTheoreticalExitsUseCase.Command(
                        id, user.getId(), ajustements != null ? ajustements : Map.of()));
        return ResponseEntity.ok(result);
    }

    /** Boulanger : valide les quantités réellement produites. */
    @PostMapping("/production/{id}/valider")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','BOULANGER')")
    public ResponseEntity<?> valider(
            @PathVariable Long id,
            @RequestBody ValidateBody body) {
        User user = userService.getCurrentUser();
        var result = validateProduction.execute(
                new ValidateProductionQuantitiesUseCase.Command(
                        id, user.getId(),
                        body != null ? body.quantitesProduits() : Map.of(),
                        body != null ? body.quantitesMatieres() : Map.of()));
        return ResponseEntity.ok(Map.of(
                "productionId", result.production().getId(),
                "statut", result.production().getStatut().name(),
                "nbIncidents", result.nbIncidentsDetectes()
        ));
    }

    public record ValidateBody(
            Map<Long, Integer> quantitesProduits,
            Map<Long, Double> quantitesMatieres
    ) {}

    /** Dashboard cycle du jour. */
    @GetMapping("/statut")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> statut(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(cycleStatus.execute(date != null ? date : LocalDate.now()));
    }
}
