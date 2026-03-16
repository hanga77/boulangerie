package lab.hang.Gestion.boulangerie.repository;

import lab.hang.Gestion.boulangerie.model.VenteLibre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface VenteLibreRepository extends JpaRepository<VenteLibre, Long> {

    List<VenteLibre> findByProductionId(Long productionId);

    List<VenteLibre> findByDateVenteBetween(LocalDate debut, LocalDate fin);

    @Query("SELECT SUM(v.montantTotal) FROM VenteLibre v WHERE v.guichet.pointDeVente.id = :pdvId")
    Double sumMontantTotalByGuichetPointDeVenteId(@Param("pdvId") Long pdvId);

    @Query("SELECT SUM(v.montantTotal) FROM VenteLibre v WHERE v.guichet.pointDeVente.id = :pdvId AND v.dateVente BETWEEN :debut AND :fin")
    Double sumMontantTotalByGuichetPointDeVenteIdAndDateBetween(@Param("pdvId") Long pdvId,
                                                                @Param("debut") LocalDate debut,
                                                                @Param("fin") LocalDate fin);

    @Query("SELECT COUNT(v) FROM VenteLibre v WHERE v.guichet.pointDeVente.id = :pdvId")
    long countByGuichetPointDeVenteId(@Param("pdvId") Long pdvId);

    @Query("SELECT COUNT(v) FROM VenteLibre v WHERE v.guichet.id = :guichetId")
    long countByGuichetId(@Param("guichetId") Long guichetId);
}
