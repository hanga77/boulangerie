package lab.hang.Gestion.boulangerie.domain.sales;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LivraisonV2Test {

    @Test
    void ajouter_lignes_cumule_montant() {
        LivraisonV2 liv = new LivraisonV2();
        liv.setNomClient("PDV Centre");
        liv.setProductionV2Id(1L);
        liv.setUserId(2L);

        liv.ajouterLigne(10L, 20, 500);
        liv.ajouterLigne(11L, 10, 300);

        assertEquals(2, liv.getLignes().size());
        assertEquals(13000.0, liv.getMontantTotal(), 0.001);
        assertSame(liv, liv.getLignes().get(0).getLivraison());
    }

    @Test
    void facture_lien_optionnel() {
        LivraisonV2 liv = new LivraisonV2();
        liv.setFactureId(99L);
        liv.setFactureNumero("FAC-20261024-0001");
        assertEquals(99L, liv.getFactureId());
        assertEquals("FAC-20261024-0001", liv.getFactureNumero());
    }
}
