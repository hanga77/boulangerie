package lab.hang.Gestion.boulangerie.domain.production;

/**
 * Étapes ordonnées du cycle journalier d'une boulangerie.
 */
public enum DailyCycleStep {
    COMMANDES("Commandes points de vente", "MANAGER"),
    PRODUCTION_LANCEE("Lancement production", "BOULANGER"),
    PRODUCTION_VALIDEE("Validation quantités produites", "BOULANGER"),
    SORTIE_STOCK("Sortie stock matières (magasinier)", "MAGASINIER"),
    LIVRAISONS("Livraisons points de vente", "MANAGER"),
    VENTES_GUICHET("Ventes au guichet", "CAISSIER"),
    CLOTURE("Clôture de la journée", "MANAGER");

    private final String label;
    private final String rolePrincipal;

    DailyCycleStep(String label, String rolePrincipal) {
        this.label = label;
        this.rolePrincipal = rolePrincipal;
    }

    public String label() {
        return label;
    }

    public String rolePrincipal() {
        return rolePrincipal;
    }

    public DailyCycleStep next() {
        int i = ordinal() + 1;
        return i < values().length ? values()[i] : null;
    }
}
