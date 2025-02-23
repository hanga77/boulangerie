package lab.hang.Gestion.boulangerie.repository;

import lab.hang.Gestion.boulangerie.model.Livraison;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public interface LivraisonRepository extends JpaRepository<Livraison, Long> {
    List<Livraison> findByDateLivraison(LocalDate date);

    List<Livraison> findByProductionId(Long productionId);

    List<Livraison> findByDateLivraisonBetween(LocalDate startDate, LocalDate endDate);
}