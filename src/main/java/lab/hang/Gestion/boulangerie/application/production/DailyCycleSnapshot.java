package lab.hang.Gestion.boulangerie.application.production;

import lab.hang.Gestion.boulangerie.domain.production.DailyCycleStep;
import lab.hang.Gestion.boulangerie.domain.production.ProductionStatus;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.Map;

/**
 * Vue synthétique de l'avancement du cycle journalier.
 */
public record DailyCycleSnapshot(
        LocalDate date,
        Long productionId,
        ProductionStatus productionStatus,
        int nbCommandes,
        int nbCommandesTraitees,
        boolean productionLancee,
        boolean productionValidee,
        boolean sortiesStockCompletes,
        int nbLignesMatiere,
        int nbLignesMatiereConfirmees,
        int nbLivraisons,
        int quantiteProduitsRestants,
        int nbVentesGuichet,
        boolean cloturee,
        DailyCycleStep etapeCourante,
        Map<DailyCycleStep, Boolean> checklist
) {
    public static Builder builder(LocalDate date) {
        return new Builder(date);
    }

    public static class Builder {
        private final LocalDate date;
        private Long productionId;
        private ProductionStatus productionStatus;
        private int nbCommandes;
        private int nbCommandesTraitees;
        private boolean productionLancee;
        private boolean productionValidee;
        private boolean sortiesStockCompletes;
        private int nbLignesMatiere;
        private int nbLignesMatiereConfirmees;
        private int nbLivraisons;
        private int quantiteProduitsRestants;
        private int nbVentesGuichet;
        private boolean cloturee;

        public Builder(LocalDate date) {
            this.date = date;
        }

        public Builder productionId(Long v) { this.productionId = v; return this; }
        public Builder productionStatus(ProductionStatus v) { this.productionStatus = v; return this; }
        public Builder nbCommandes(int v) { this.nbCommandes = v; return this; }
        public Builder nbCommandesTraitees(int v) { this.nbCommandesTraitees = v; return this; }
        public Builder productionLancee(boolean v) { this.productionLancee = v; return this; }
        public Builder productionValidee(boolean v) { this.productionValidee = v; return this; }
        public Builder sortiesStockCompletes(boolean v) { this.sortiesStockCompletes = v; return this; }
        public Builder nbLignesMatiere(int v) { this.nbLignesMatiere = v; return this; }
        public Builder nbLignesMatiereConfirmees(int v) { this.nbLignesMatiereConfirmees = v; return this; }
        public Builder nbLivraisons(int v) { this.nbLivraisons = v; return this; }
        public Builder quantiteProduitsRestants(int v) { this.quantiteProduitsRestants = v; return this; }
        public Builder nbVentesGuichet(int v) { this.nbVentesGuichet = v; return this; }
        public Builder cloturee(boolean v) { this.cloturee = v; return this; }

        public DailyCycleSnapshot build() {
            Map<DailyCycleStep, Boolean> checklist = new EnumMap<>(DailyCycleStep.class);
            checklist.put(DailyCycleStep.COMMANDES, nbCommandes > 0);
            checklist.put(DailyCycleStep.PRODUCTION_LANCEE, productionLancee);
            checklist.put(DailyCycleStep.PRODUCTION_VALIDEE, productionValidee);
            checklist.put(DailyCycleStep.SORTIE_STOCK, sortiesStockCompletes);
            checklist.put(DailyCycleStep.LIVRAISONS, nbLivraisons > 0);
            checklist.put(DailyCycleStep.VENTES_GUICHET, nbVentesGuichet > 0 || quantiteProduitsRestants == 0);
            checklist.put(DailyCycleStep.CLOTURE, cloturee);

            DailyCycleStep courante = DailyCycleStep.COMMANDES;
            for (DailyCycleStep step : DailyCycleStep.values()) {
                if (!Boolean.TRUE.equals(checklist.get(step))) {
                    courante = step;
                    break;
                }
                if (step == DailyCycleStep.CLOTURE) {
                    courante = DailyCycleStep.CLOTURE;
                }
            }

            return new DailyCycleSnapshot(
                    date, productionId, productionStatus,
                    nbCommandes, nbCommandesTraitees,
                    productionLancee, productionValidee, sortiesStockCompletes,
                    nbLignesMatiere, nbLignesMatiereConfirmees,
                    nbLivraisons, quantiteProduitsRestants, nbVentesGuichet, cloturee,
                    courante, checklist
            );
        }
    }
}
