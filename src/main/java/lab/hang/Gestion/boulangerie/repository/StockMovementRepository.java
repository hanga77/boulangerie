package lab.hang.Gestion.boulangerie.repository;

import lab.hang.Gestion.boulangerie.model.MatierePremiere;
import lab.hang.Gestion.boulangerie.model.StockMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    List<StockMovement> findByDateOrderByDateDesc(LocalDate date);

    int countByDateAndType(LocalDate now, String entree);

    List<StockMovement> findByDate(LocalDate date);

    @Query("SELECT m FROM StockMovement m WHERE m.date BETWEEN :start AND :end ORDER BY m.date DESC")
    List<StockMovement> findByDateBetween(@Param("start") LocalDate start,
                                          @Param("end") LocalDate end);

    @Query("SELECT m FROM StockMovement m WHERE m.date BETWEEN :start AND :end")
    Page<StockMovement> findByDateBetween(@Param("start") LocalDate start,
                                          @Param("end") LocalDate end,
                                          Pageable pageable);

    List<StockMovement> findByDateAndMatierePremiere(LocalDate date, MatierePremiere matierePremiere);

    List<StockMovement> findByDateAndMatierePremiereAndType(LocalDate date, MatierePremiere matierePremiere, String type);

    @Query("SELECT m FROM StockMovement m LEFT JOIN FETCH m.matierePremiere LEFT JOIN FETCH m.production LEFT JOIN FETCH m.user ORDER BY m.date DESC, m.id DESC")
    List<StockMovement> findAllWithAssociationsOrderByDateDesc();

    @Query("SELECT m FROM StockMovement m JOIN FETCH m.matierePremiere WHERE m.production.id = :productionId AND m.type = 'SORTIE'")
    List<StockMovement> findSortiesByProductionId(@Param("productionId") Long productionId);
}