package lab.hang.Gestion.boulangerie.infrastructure.persistence;

import lab.hang.Gestion.boulangerie.domain.sales.LivraisonV2;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LivraisonV2Repository extends JpaRepository<LivraisonV2, Long> {
    List<LivraisonV2> findByProductionV2Id(Long productionV2Id);
    int countByProductionV2Id(Long productionV2Id);
}
