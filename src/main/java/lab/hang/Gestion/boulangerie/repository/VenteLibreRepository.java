package lab.hang.Gestion.boulangerie.repository;

import lab.hang.Gestion.boulangerie.model.VenteLibre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface VenteLibreRepository extends JpaRepository<VenteLibre, Long> {
    List<VenteLibre> findByProductionId(Long productionId);
    List<VenteLibre> findByDateVenteBetween(LocalDate debut, LocalDate fin);
}
