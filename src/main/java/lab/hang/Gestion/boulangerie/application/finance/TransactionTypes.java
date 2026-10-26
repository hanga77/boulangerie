package lab.hang.Gestion.boulangerie.application.finance;

import java.util.List;
import java.util.Set;

/**
 * Vocabulaire unique des types de transaction trésorerie.
 */
public final class TransactionTypes {

    private TransactionTypes() {}

    public static final String VENTE = "VENTE";
    public static final String VENTE_LIBRE = "VENTE_LIBRE";
    public static final String VENTE_LIVRAISON_V2 = "VENTE_LIVRAISON_V2";
    public static final String VENTE_CAISSE_V2 = "VENTE_CAISSE_V2";
    public static final String PAIEMENT_FACTURE = "PAIEMENT_FACTURE";

    public static final String ACHAT = "ACHAT";
    public static final String PRODUCTION = "PRODUCTION";
    public static final String SALAIRE = "SALAIRE";
    public static final String SALAIRE_ENFOIS = "SALAIRE_ENFOIS";
    /** Paiement charge fixe (loyer, électricité…). */
    public static final String CHARGE = "CHARGE";
    /** Remboursement dette fournisseur. */
    public static final String DETTE_REMBOURSEMENT = "DETTE_REMBOURSEMENT";

    public static final List<String> REVENUE_TYPES = List.of(
            VENTE, VENTE_LIBRE, VENTE_LIVRAISON_V2, VENTE_CAISSE_V2, PAIEMENT_FACTURE
    );

    public static final Set<String> REVENUE_SET = Set.copyOf(REVENUE_TYPES);

    public static final List<String> EXPENSE_TYPES = List.of(
            ACHAT, PRODUCTION, SALAIRE, SALAIRE_ENFOIS, CHARGE, DETTE_REMBOURSEMENT
    );

    public static boolean isRevenue(String type) {
        return type != null && REVENUE_SET.contains(type);
    }
}
