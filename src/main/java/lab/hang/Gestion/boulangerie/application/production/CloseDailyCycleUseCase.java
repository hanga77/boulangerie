package lab.hang.Gestion.boulangerie.application.production;

import lab.hang.Gestion.boulangerie.domain.production.ProductionStatus;
import lab.hang.Gestion.boulangerie.domain.production.ProductionV2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Dernière étape du cycle : clôture de la journée.
 * Vérifie que les étapes critiques sont faites, puis marque la production CLOTUREE.
 */
@Service
public class CloseDailyCycleUseCase {

    private final GetDailyCycleStatusUseCase statusUseCase;
    private final ProductionPersistencePort persistencePort;
    private final DailyCycleQueryPort queryPort;

    public CloseDailyCycleUseCase(GetDailyCycleStatusUseCase statusUseCase,
                                  ProductionPersistencePort persistencePort,
                                  DailyCycleQueryPort queryPort) {
        this.statusUseCase = statusUseCase;
        this.persistencePort = persistencePort;
        this.queryPort = queryPort;
    }

    public record Command(LocalDate date, Long userId, boolean forcer) {}

    public record Result(DailyCycleSnapshot snapshot, String message) {}

    @Transactional
    public Result execute(Command cmd) {
        LocalDate jour = cmd.date() != null ? cmd.date() : LocalDate.now();

        if (queryPort.isJourCloture(jour)) {
            throw new IllegalStateException("La journée " + jour + " est déjà clôturée");
        }

        DailyCycleSnapshot snap = statusUseCase.execute(jour);

        if (!snap.productionLancee()) {
            throw new IllegalStateException("Impossible de clôturer : aucune production lancée");
        }
        if (!snap.productionValidee() && !cmd.forcer()) {
            throw new IllegalStateException(
                    "Production non validée. Validez les quantités ou utilisez forcer=true");
        }
        if (!snap.sortiesStockCompletes() && !cmd.forcer()) {
            throw new IllegalStateException(
                    "Sorties stock incomplètes ("
                            + snap.nbLignesMatiereConfirmees() + "/"
                            + snap.nbLignesMatiere()
                            + "). Terminez les sorties magasinier ou forcer=true");
        }

        ProductionV2 production = persistencePort.getById(snap.productionId());
        if (production.getStatut() == ProductionStatus.VALIDEE) {
            production.cloturer();
            persistencePort.save(production);
        } else if (production.getStatut() == ProductionStatus.EN_COURS && cmd.forcer()) {
            production.valider();
            production.cloturer();
            persistencePort.save(production);
        }

        DailyCycleSnapshot apres = statusUseCase.execute(jour);
        return new Result(apres, "Journée " + jour + " clôturée avec succès");
    }
}
