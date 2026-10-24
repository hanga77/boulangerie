package lab.hang.Gestion.boulangerie.infrastructure.persistence;

import lab.hang.Gestion.boulangerie.domain.sales.LivraisonV2;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LivraisonV2Repository extends JpaRepository<LivraisonV2, Long> {
    List<LivraisonV2> findByProductionV2Id(Long productionV2Id);
    int countByProductionV2Id(Long productionV2Id);

    @Query("SELECT DISTINCT l FROM LivraisonV2 l LEFT JOIN FETCH l.lignes WHERE l.id = :id")
    Optional<LivraisonV2> findByIdWithLignes(@Param("id") Long id);
}
