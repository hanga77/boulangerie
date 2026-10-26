package lab.hang.Gestion.boulangerie.infrastructure.persistence;

import lab.hang.Gestion.boulangerie.domain.production.ProductionV2;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ProductionV2Repository extends JpaRepository<ProductionV2, Long> {

    boolean existsByDateProduction(LocalDate dateProduction);

    List<ProductionV2> findByDateProduction(LocalDate dateProduction);

    @Query("SELECT DISTINCT p FROM ProductionV2 p " +
           "LEFT JOIN FETCH p.lignesProduits " +
           "LEFT JOIN FETCH p.lignesMatieres " +
           "WHERE p.dateProduction = :date")
    Optional<ProductionV2> findByDateProductionWithLignes(@Param("date") LocalDate date);

    @Query("SELECT DISTINCT p FROM ProductionV2 p " +
           "LEFT JOIN FETCH p.lignesProduits " +
           "LEFT JOIN FETCH p.lignesMatieres " +
           "WHERE p.id = :id")
    Optional<ProductionV2> findByIdWithLignes(@Param("id") Long id);
}
