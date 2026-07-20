package lab.hang.Gestion.boulangerie.repository;

import lab.hang.Gestion.boulangerie.model.Livraison;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public interface LivraisonRepository extends JpaRepository<Livraison, Long> {
    List<Livraison> findByDateLivraison(LocalDate date);

    List<Livraison> findByProductionId(Long productionId);

    List<Livraison> findByDateLivraisonBetween(LocalDate startDate, LocalDate endDate);

    List<Livraison> findByPointDeVenteIdOrderByDateLivraisonDesc(Long pointDeVenteId);

    @Query("SELECT COALESCE(SUM(l.montantTotal), 0.0) FROM Livraison l WHERE l.pointDeVente.id = :pointDeVenteId")
    double sumMontantByPointDeVenteId(@Param("pointDeVenteId") Long pointDeVenteId);
}