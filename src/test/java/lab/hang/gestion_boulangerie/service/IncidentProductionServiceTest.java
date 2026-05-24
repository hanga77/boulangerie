package lab.hang.gestion_boulangerie.service;

import lab.hang.Gestion.boulangerie.model.*;
import lab.hang.Gestion.boulangerie.repository.IncidentProductionRepository;
import lab.hang.Gestion.boulangerie.service.IncidentProductionService;
import lab.hang.Gestion.boulangerie.service.StockService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IncidentProductionServiceTest {

    @Mock IncidentProductionRepository incidentRepository;
    @Mock StockService stockService;
    @InjectMocks IncidentProductionService incidentService;

    @Test
    void creerIncident_MATIERE_AVARIEE_deduitStock() {
        Production production = new Production();
        production.setId(1L);
        production.setDateProduction(LocalDate.now());

        User boulanger = new User();
        boulanger.setId(1L);

        MatierePremiere farine = new MatierePremiere();
        farine.setId(2L);
        farine.setNom("Farine de blé");

        StockMovement movement = new StockMovement();
        movement.setId(10L);
        when(stockService.lostStock(2L, 5.0, "AVARIE: cause test")).thenReturn(movement);
        when(incidentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        IncidentProduction incident = incidentService.creerIncident(
                production, TypeIncident.MATIERE_AVARIEE, null, farine,
                5.0, "cause test", boulanger
        );

        assertThat(incident.isStockAjuste()).isTrue();
        assertThat(incident.getMouvementStock()).isEqualTo(movement);
        verify(stockService).lostStock(2L, 5.0, "AVARIE: cause test");
    }

    @Test
    void creerIncident_FOURNEE_BRULEE_neDeduitPasStock() {
        Production production = new Production();
        production.setId(1L);
        production.setDateProduction(LocalDate.now());

        User boulanger = new User();
        boulanger.setId(1L);

        Produit croissant = new Produit();
        croissant.setId(3L);

        when(incidentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        IncidentProduction incident = incidentService.creerIncident(
                production, TypeIncident.FOURNEE_BRULEE, croissant, null,
                30.0, "four trop chaud", boulanger
        );

        assertThat(incident.isStockAjuste()).isFalse();
        assertThat(incident.getMouvementStock()).isNull();
        verifyNoInteractions(stockService);
    }

    @Test
    void getIncidentsByProduction_delegatesRepository() {
        when(incidentRepository.findByProductionId(1L)).thenReturn(List.of());
        List<IncidentProduction> result = incidentService.getIncidentsByProduction(1L);
        assertThat(result).isEmpty();
        verify(incidentRepository).findByProductionId(1L);
    }
}
