package lab.hang.Gestion.boulangerie.domain.sales;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VenteLibreV2Test {

    @Test
    void ajouter_lignes_cumule_montant_et_moyen_paiement() {
        VenteLibreV2 v = new VenteLibreV2();
        v.setProductionV2Id(1L);
        v.setUserId(2L);
        v.setMoyenPaiement("MOBILE_MONEY");

        v.ajouterLigne(10L, 3, 500);
        v.ajouterLigne(11L, 2, 250);

        assertEquals(2, v.getLignes().size());
        assertEquals(2000.0, v.getMontantTotal(), 0.001);
        assertEquals("MOBILE_MONEY", v.getMoyenPaiement());
        assertSame(v, v.getLignes().get(0).getVente());
    }
}
