package lab.hang.Gestion.boulangerie.repository;

import lab.hang.Gestion.boulangerie.model.Production;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ProductionRepository extends JpaRepository<Production, Long> {
    List<Production> findByDateProduction(LocalDate date);

    List<Production> findByDateProductionBetween(LocalDate startDate, LocalDate endDate);

    Page<Production> findByDateProductionBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);

}
