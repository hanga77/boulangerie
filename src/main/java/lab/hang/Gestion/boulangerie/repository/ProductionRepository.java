package lab.hang.Gestion.boulangerie.repository;

import lab.hang.Gestion.boulangerie.model.Production;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ProductionRepository extends JpaRepository<Production, Long> {
    List<Production> findByDateProduction(LocalDate date);

    List<Production> findByDateProductionBetween(LocalDate startDate, LocalDate endDate);

    @Query(value = "SELECT DISTINCT p FROM Production p LEFT JOIN FETCH p.produitsProduits WHERE p.dateProduction BETWEEN :start AND :end",
           countQuery = "SELECT COUNT(p) FROM Production p WHERE p.dateProduction BETWEEN :start AND :end")
    Page<Production> findByDateProductionBetween(@Param("start") LocalDate start, @Param("end") LocalDate end, Pageable pageable);

}
