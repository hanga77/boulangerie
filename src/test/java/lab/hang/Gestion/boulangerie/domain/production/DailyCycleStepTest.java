package lab.hang.Gestion.boulangerie.domain.production;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Vérifie que l'enum des étapes du cycle journalier reste cohérent.
 */
class DailyCycleStepTest {

    @Test
    void toutes_les_etapes_ont_label_et_role() {
        for (DailyCycleStep step : DailyCycleStep.values()) {
            assertNotNull(step.label());
            assertFalse(step.label().isBlank());
            assertNotNull(step.rolePrincipal());
            assertFalse(step.rolePrincipal().isBlank());
        }
    }

    @Test
    void ordre_metier_commence_par_commandes() {
        DailyCycleStep[] steps = DailyCycleStep.values();
        assertEquals(DailyCycleStep.COMMANDES, steps[0]);
    }
}
