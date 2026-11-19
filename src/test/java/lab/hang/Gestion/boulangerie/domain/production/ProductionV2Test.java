package lab.hang.Gestion.boulangerie.domain.production;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires domaine ProductionV2 (sans Spring / BDD).
 */
class ProductionV2Test {

    @Test
    void creation_met_statut_en_cours() {
        ProductionV2 p = new ProductionV2(LocalDate.of(2026, 10, 24), 1L);
        assertEquals(ProductionStatus.EN_COURS, p.getStatut());
        assertEquals(LocalDate.of(2026, 10, 24), p.getDateProduction());
    }

    @Test
    void ajouter_lignes_produit_et_matiere() {
        ProductionV2 p = new ProductionV2(LocalDate.now(), 1L);
        p.ajouterLigneProduit(10L, 100);
        p.ajouterOuCumulerMatiere(1L, 25.5);
        p.ajouterOuCumulerMatiere(1L, 4.5); // cumul farinage

        assertEquals(1, p.getLignesProduits().size());
        assertEquals(100, p.getLignesProduits().get(0).getQuantitePlanifiee());
        assertEquals(100, p.getLignesProduits().get(0).getQuantiteRestante());

        assertEquals(1, p.getLignesMatieres().size());
        assertEquals(30.0, p.getLignesMatieres().get(0).getQuantiteTheorique(), 0.001);
    }

    @Test
    void valider_puis_cloturer() {
        ProductionV2 p = new ProductionV2(LocalDate.now(), 1L);
        p.valider();
        assertEquals(ProductionStatus.VALIDEE, p.getStatut());
        p.cloturer();
        assertEquals(ProductionStatus.CLOTUREE, p.getStatut());
    }

    @Test
    void valider_refuse_si_deja_validee() {
        ProductionV2 p = new ProductionV2(LocalDate.now(), 1L);
        p.valider();
        assertThrows(IllegalStateException.class, p::valider);
    }

    @Test
    void cloturer_refuse_si_pas_validee() {
        ProductionV2 p = new ProductionV2(LocalDate.now(), 1L);
        assertThrows(IllegalStateException.class, p::cloturer);
    }

    @Test
    void debiter_restant_ok_et_insuffisant() {
        ProductionV2 p = new ProductionV2(LocalDate.now(), 1L);
        p.ajouterLigneProduit(5L, 50);
        ProductionLigneProduit ligne = p.ligneProduit(5L).orElseThrow();

        ligne.debiterRestant(20);
        assertEquals(30, ligne.getQuantiteRestante());

        assertThrows(IllegalArgumentException.class, () -> ligne.debiterRestant(40));
    }

    @Test
    void valider_quantite_reelle_maj_restante() {
        ProductionV2 p = new ProductionV2(LocalDate.now(), 1L);
        p.ajouterLigneProduit(7L, 80);
        ProductionLigneProduit ligne = p.ligneProduit(7L).orElseThrow();

        ligne.validerQuantiteReelle(75);
        assertEquals(75, ligne.getQuantiteReelle());
        assertEquals(75, ligne.getQuantiteRestante());
    }
}
