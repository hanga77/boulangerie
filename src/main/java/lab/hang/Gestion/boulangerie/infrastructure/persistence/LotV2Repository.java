package lab.hang.Gestion.boulangerie.infrastructure.persistence;

import lab.hang.Gestion.boulangerie.domain.inventory.LotV2;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LotV2Repository extends JpaRepository<LotV2, Long> {
    List<LotV2> findByMatierePremiereIdOrderByDateEntreeAsc(Long matierePremiereId);
}
