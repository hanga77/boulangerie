package lab.hang.Gestion.boulangerie.model;

/**
 * Codes comptables du plan comptable général utilisés dans l'application.
 */
public enum CompteComptable {
    BANQUE("512", "Banque"),
    VENTES_MARCHANDISES("707", "Ventes de marchandises"),
    ACHATS_MARCHANDISES("607", "Achats de marchandises"),
    COMPTE_ATTENTE("471", "Compte d'attente");

    private final String code;
    private final String libelle;

    CompteComptable(String code, String libelle) {
        this.code = code;
        this.libelle = libelle;
    }

    public String getCode() { return code; }
    public String getLibelle() { return libelle; }
}
