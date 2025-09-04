package lab.hang.Gestion.boulangerie.repository;

import lab.hang.Gestion.boulangerie.model.KPI;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface KPIRepository extends JpaRepository<KPI, Long> {
    List<KPI> findByDateAndCategorie(LocalDate date, String categorie);
    List<KPI> findByNomAndDateBetween(String nom, LocalDate debut, LocalDate fin);
}
