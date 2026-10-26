package lab.hang.Gestion.boulangerie.application.finance;

import java.util.List;
import java.util.Set;

/**
 * Phase 6 — vocabulaire unique des types de transaction trésorerie.
 * Compatible avec l'historique V1 (VENTE, ACHAT, PRODUCTION…).
 */
public final class TransactionTypes {

    private TransactionTypes() {}

    /** Vente / livraison historique V1 (et anciennes livraisons V2 avant normalisation). */
    public static final String VENTE = "VENTE";

    /** Ancien type caisse libre (encore présent en base). */
    public static final String VENTE_LIBRE = "VENTE_LIBRE";

    /** Livraison cycle V2. */
    public static final String VENTE_LIVRAISON_V2 = "VENTE_LIVRAISON_V2";

    /** Caisse / vente libre cycle V2. */
    public static final String VENTE_CAISSE_V2 = "VENTE_CAISSE_V2";

    public static final String ACHAT = "ACHAT";
    public static final String PRODUCTION = "PRODUCTION";
    public static final String PAIEMENT_FACTURE = "PAIEMENT_FACTURE";
    public static final String SALAIRE = "SALAIRE";
    public static final String SALAIRE_ENFOIS = "SALAIRE_ENFOIS";

    /** Tous les types comptés comme chiffre d'affaires (crédit compte). */
    public static final List<String> REVENUE_TYPES = List.of(
            VENTE,
            VENTE_LIBRE,
            VENTE_LIVRAISON_V2,
            VENTE_CAISSE_V2,
            PAIEMENT_FACTURE
    );

    public static final Set<String> REVENUE_SET = Set.copyOf(REVENUE_TYPES);

    public static final List<String> EXPENSE_TYPES = List.of(
            ACHAT, PRODUCTION, SALAIRE, SALAIRE_ENFOIS
    );

    public static boolean isRevenue(String type) {
        return type != null && REVENUE_SET.contains(type);
    }
}
