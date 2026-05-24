package lab.hang.Gestion.boulangerie.repository;

import lab.hang.Gestion.boulangerie.model.AppSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppSettingsRepository extends JpaRepository<AppSettings, String> {}
