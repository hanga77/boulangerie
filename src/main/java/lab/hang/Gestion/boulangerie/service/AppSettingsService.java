package lab.hang.Gestion.boulangerie.service;

import lab.hang.Gestion.boulangerie.model.AppSettings;
import lab.hang.Gestion.boulangerie.repository.AppSettingsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppSettingsService {

    private static final String SEUIL_KEY = "seuil_incident_production";
    private static final double SEUIL_DEFAULT = 10.0;

    private final AppSettingsRepository appSettingsRepository;

    public AppSettingsService(AppSettingsRepository appSettingsRepository) {
        this.appSettingsRepository = appSettingsRepository;
    }

    public double getSeuilIncident() {
        return appSettingsRepository.findById(SEUIL_KEY)
                .map(s -> {
                    try { return Double.parseDouble(s.getValeur()); }
                    catch (NumberFormatException e) { return SEUIL_DEFAULT; }
                })
                .orElse(SEUIL_DEFAULT);
    }

    @Transactional
    public void updateSeuilIncident(double seuil) {
        if (!Double.isFinite(seuil) || seuil <= 0) {
            throw new IllegalArgumentException("Seuil invalide : " + seuil);
        }
        appSettingsRepository.save(new AppSettings(SEUIL_KEY, String.valueOf(seuil)));
    }
}
