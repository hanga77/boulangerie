package lab.hang.Gestion.boulangerie.application.inventory;

/**
 * Ligne de la feuille de sortie magasinier, calculée au lancement de production.
 * Inclut les matières des recettes (commandes + vente libre) ET le farinage du lot.
 */
public record MatiereAFournir(
        Long matierePremiereId,
        String nom,
        String uniteMesure,
        double quantiteTheorique,
        double stockActuel,
        Double quantiteDejaSortie,
        boolean farinage
) {
    /** Quantité encore à sortir (théorique − déjà confirmé par le magasinier). */
    public double quantiteRestanteASortir() {
        double deja = quantiteDejaSortie != null ? quantiteDejaSortie : 0;
        return Math.max(0, quantiteTheorique - deja);
    }

    public boolean stockSuffisant() {
        return stockActuel >= quantiteRestanteASortir();
    }
}
