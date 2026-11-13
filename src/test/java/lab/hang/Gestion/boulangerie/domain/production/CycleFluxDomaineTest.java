package lab.hang.Gestion.boulangerie.domain.production;

import lab.hang.Gestion.boulangerie.domain.sales.LivraisonV2;
import lab.hang.Gestion.boulangerie.domain.sales.VenteLibreV2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase 5 P2 — scénario métier sans Spring :
 * production validée → débit restants → livraison / vente → montants cohérents.
 */
class CycleFluxDomaineTest {

    private ProductionV2 production;

    @BeforeEach
    void setUp() {
        production = new ProductionV2(LocalDate.of(2026, 10, 26), 1L);
        production.ajouterLigneProduit(10L, 100); // pain
        production.ajouterLigneProduit(20L, 40);  // baguette
        production.ajouterOuCumulerMatiere(1L, 50.0); // farine recettes
        production.ajouterOuCumulerMatiere(1L, 2.5);  // farinage
        production.valider();
    }

    @Test
    void production_validee_restants_egaux_au_reel() {
        assertEquals(ProductionStatus.VALIDEE, production.getStatut());
        assertEquals(100, production.ligneProduit(10L).orElseThrow().getQuantiteRestante());
        assertEquals(40, production.ligneProduit(20L).orElseThrow().getQuantiteRestante());
        assertEquals(52.5, production.ligneMatiere(1L).orElseThrow().getQuantiteTheorique(), 0.001);
    }

    @Test
    void magasinier_confirme_puis_livraison_debite_restants() {
        production.ligneMatiere(1L).orElseThrow().confirmerMagasinier(52.0);

        LivraisonV2 liv = new LivraisonV2();
        liv.setProductionV2Id(production.getId() != null ? production.getId() : 1L);
        liv.setNomClient("PDV Centre");
        liv.setUserId(1L);

        // 30 pains à 500, 10 baguettes à 300
        production.ligneProduit(10L).orElseThrow().debiterRestant(30);
        liv.ajouterLigne(10L, 30, 500);

        production.ligneProduit(20L).orElseThrow().debiterRestant(10);
        liv.ajouterLigne(20L, 10, 300);

        assertEquals(70, production.ligneProduit(10L).orElseThrow().getQuantiteRestante());
        assertEquals(30, production.ligneProduit(20L).orElseThrow().getQuantiteRestante());
        assertEquals(18000.0, liv.getMontantTotal(), 0.001); // 30*500 + 10*300
        assertEquals(2, liv.getLignes().size());
    }

    @Test
    void vente_caisse_debite_et_cumule_moyen_paiement() {
        VenteLibreV2 vente = new VenteLibreV2();
        vente.setProductionV2Id(1L);
        vente.setUserId(5L);
        vente.setMoyenPaiement("ESPECES");

        production.ligneProduit(10L).orElseThrow().debiterRestant(5);
        vente.ajouterLigne(10L, 5, 500);

        assertEquals(95, production.ligneProduit(10L).orElseThrow().getQuantiteRestante());
        assertEquals(2500.0, vente.getMontantTotal(), 0.001);
        assertEquals("ESPECES", vente.getMoyenPaiement());
    }

    @Test
    void debit_excedentaire_refuse() {
        ProductionLigneProduit ligne = production.ligneProduit(20L).orElseThrow();
        assertThrows(IllegalArgumentException.class, () -> ligne.debiterRestant(41));
        assertEquals(40, ligne.getQuantiteRestante());
    }

    @Test
    void flux_complet_restants_puis_cloture() {
        production.ligneProduit(10L).orElseThrow().debiterRestant(100);
        production.ligneProduit(20L).orElseThrow().debiterRestant(40);

        assertEquals(0, production.ligneProduit(10L).orElseThrow().getQuantiteRestante());
        assertEquals(0, production.ligneProduit(20L).orElseThrow().getQuantiteRestante());

        production.cloturer();
        assertEquals(ProductionStatus.CLOTUREE, production.getStatut());
    }

    @Test
    void ajustement_quantite_reelle_avant_vente() {
        ProductionV2 p = new ProductionV2(LocalDate.now(), 1L);
        p.ajouterLigneProduit(10L, 100);
        p.ligneProduit(10L).orElseThrow().validerQuantiteReelle(90);
        p.valider();

        assertEquals(90, p.ligneProduit(10L).orElseThrow().getQuantiteRestante());

        p.ligneProduit(10L).orElseThrow().debiterRestant(90);
        assertEquals(0, p.ligneProduit(10L).orElseThrow().getQuantiteRestante());
    }
}
