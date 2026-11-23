package lab.hang.Gestion.boulangerie.application.inventory;

/**
 * Commande d'entrée de stock (réception fournisseur).
 */
public record ReceiveStockCommand(
        Long matierePremiereId,
        double quantiteCommandee,
        double quantiteAvariee,
        double prixUnitaire,
        Integer dureeConservationJours,
        Long fournisseurId,
        Long userId,
        String username
) {
    public double quantiteNette() {
        double avariee = quantiteAvariee > 0 ? quantiteAvariee : 0;
        return quantiteCommandee - avariee;
    }
}
