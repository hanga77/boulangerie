package lab.hang.gestion_boulangerie.service;

import lab.hang.Gestion.boulangerie.model.AppSettings;
import lab.hang.Gestion.boulangerie.repository.AppSettingsRepository;
import lab.hang.Gestion.boulangerie.service.AppSettingsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppSettingsServiceTest {

    @Mock AppSettingsRepository appSettingsRepository;
    @InjectMocks AppSettingsService appSettingsService;

    @Test
    void getSeuil_returnsStoredValue() {
        AppSettings setting = new AppSettings();
        setting.setCle("seuil_incident_production");
        setting.setValeur("15.0");
        when(appSettingsRepository.findById("seuil_incident_production"))
                .thenReturn(Optional.of(setting));

        double seuil = appSettingsService.getSeuilIncident();

        assertThat(seuil).isEqualTo(15.0);
    }

    @Test
    void getSeuil_returnsDefault10WhenAbsent() {
        when(appSettingsRepository.findById("seuil_incident_production"))
                .thenReturn(Optional.empty());

        double seuil = appSettingsService.getSeuilIncident();

        assertThat(seuil).isEqualTo(10.0);
    }

    @Test
    void updateSeuil_savesNewValue() {
        appSettingsService.updateSeuilIncident(20.0);

        verify(appSettingsRepository).save(argThat(s ->
            s.getCle().equals("seuil_incident_production") &&
            s.getValeur().equals("20.0")
        ));
    }

    @Test
    void updateSeuil_rejectsInvalidInput() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
            () -> appSettingsService.updateSeuilIncident(-5.0));
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
            () -> appSettingsService.updateSeuilIncident(0));
    }
}
