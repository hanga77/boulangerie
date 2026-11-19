package lab.hang.Gestion.boulangerie.domain.identity;

/**
 * Rôles applicatifs. Remplace le String libre de l'ancien modèle User.
 */
public enum Role {
    SUPERADMIN,
    ADMIN,
    MANAGER,
    BOULANGER,
    MAGASINIER,
    CAISSIER,
    POINT_DE_VENTE;

    public String authority() {
        return "ROLE_" + name();
    }

    public static Role fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Rôle vide");
        }
        String normalized = value.startsWith("ROLE_") ? value.substring(5) : value;
        return Role.valueOf(normalized.toUpperCase());
    }
}
