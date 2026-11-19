package lab.hang.Gestion.boulangerie.application.inventory;

import lab.hang.Gestion.boulangerie.domain.inventory.LotV2;

/**
 * Port sortant — implémenté par l'infrastructure JPA.
 */
public interface InventoryPort {

    void saveLot(LotV2 lot);

    void augmenterStock(Long matiereId, double quantite, double prixUnitaireEntree);

    void diminuerStockAvecVerrou(Long matiereId, double quantite);

    void enregistrerMouvementEntree(Long matiereId, double quantiteNette,
                                    double quantiteCommandee, double quantiteAvariee,
                                    Long userId);

    void enregistrerMouvementSortieProduction(Long matiereId, double quantite,
                                              Long productionId, Long userId);

    String nomMatiere(Long matiereId);

    /** Unité de mesure (kg, g, L…) pour affichage feuille de sortie. */
    String uniteMesure(Long matiereId);

    double stockActuel(Long matiereId);

    int dureeConservationDefaut(Long matiereId);
}
