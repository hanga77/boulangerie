package lab.hang.Gestion.boulangerie.repository;

import lab.hang.Gestion.boulangerie.model.IncidentProduction;
import lab.hang.Gestion.boulangerie.model.TypeIncident;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface IncidentProductionRepository extends JpaRepository<IncidentProduction, Long> {
    List<IncidentProduction> findByProductionId(Long productionId);
    List<IncidentProduction> findByType(TypeIncident type);
    List<IncidentProduction> findByDateIncidentBetween(LocalDate debut, LocalDate fin);
    List<IncidentProduction> findByDateIncidentBetweenAndType(LocalDate debut, LocalDate fin, TypeIncident type);
}
