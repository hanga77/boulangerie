package lab.hang.Gestion.boulangerie.application.production;

import lab.hang.Gestion.boulangerie.domain.production.ProductionLigneMatiere;
import lab.hang.Gestion.boulangerie.domain.production.ProductionLigneProduit;
import lab.hang.Gestion.boulangerie.domain.production.ProductionStatus;
import lab.hang.Gestion.boulangerie.domain.production.ProductionV2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Retourne l'état d'avancement du cycle journalier pour une date donnée.
 * À brancher sur /dashboard ou /production/cycle-du-jour.
 */
@Service
public class GetDailyCycleStatusUseCase {

    private final DailyCycleQueryPort queryPort;

    public GetDailyCycleStatusUseCase(DailyCycleQueryPort queryPort) {
        this.queryPort = queryPort;
    }

    @Transactional(readOnly = true)
    public DailyCycleSnapshot execute(LocalDate date) {
        LocalDate jour = date != null ? date : LocalDate.now();

        int nbCmd = queryPort.countCommandes(jour);
        int nbCmdTraitees = queryPort.countCommandesTraitees(jour);

        var productionOpt = queryPort.findProductionDuJour(jour);

        DailyCycleSnapshot.Builder b = DailyCycleSnapshot.builder(jour)
                .nbCommandes(nbCmd)
                .nbCommandesTraitees(nbCmdTraitees)
                .nbVentesGuichet(queryPort.countVentesGuichet(jour))
                .cloturee(queryPort.isJourCloture(jour));

        if (productionOpt.isEmpty()) {
            return b.productionLancee(false).productionValidee(false)
                    .sortiesStockCompletes(false).build();
        }

        ProductionV2 p = productionOpt.get();
        int lignesMat = p.getLignesMatieres().size();
        int confirmees = (int) p.getLignesMatieres().stream()
                .filter(l -> l.getQuantiteMagasinier() != null)
                .count();
        int restants = p.getLignesProduits().stream()
                .mapToInt(ProductionLigneProduit::getQuantiteRestante)
                .sum();

        boolean validee = p.getStatut() == ProductionStatus.VALIDEE
                || p.getStatut() == ProductionStatus.CLOTUREE;
        boolean sortiesOk = lignesMat > 0 && confirmees == lignesMat;

        return b
                .productionId(p.getId())
                .productionStatus(p.getStatut())
                .productionLancee(true)
                .productionValidee(validee)
                .nbLignesMatiere(lignesMat)
                .nbLignesMatiereConfirmees(confirmees)
                .sortiesStockCompletes(sortiesOk)
                .nbLivraisons(queryPort.countLivraisonsPourProduction(p.getId()))
                .quantiteProduitsRestants(restants)
                .build();
    }
}
