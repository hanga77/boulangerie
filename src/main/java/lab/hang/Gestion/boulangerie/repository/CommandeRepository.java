package lab.hang.Gestion.boulangerie.repository;

import lab.hang.Gestion.boulangerie.model.Commande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface CommandeRepository extends JpaRepository<Commande, Long> {

    List<Commande> findByDateCommande(LocalDate date);

    @Query("SELECT COUNT(c) FROM Commande c WHERE c.dateCommande = :date")
    int countByDateCommande(@Param("date") LocalDate date);

    @Query("SELECT SUM(c.coutTotal) FROM Commande c WHERE c.dateCommande = :date")
    Double sumCoutTotalByDateCommande(@Param("date") LocalDate date);

    @Query("SELECT COUNT(DISTINCT c.pointDeVente.id) FROM Commande c WHERE c.dateCommande = :date AND c.pointDeVente IS NOT NULL")
    Long countDistinctPointDeVenteByDateCommande(@Param("date") LocalDate date);

    boolean existsByDateCommande(LocalDate date);

    List<Commande> findByProcessedFalse();

    List<Commande> findByDateCommandeAndProcessed(LocalDate date, boolean processed);

    List<Commande> findByProductionId(Long productionId);

    // ─── Requêtes par point de vente ──────────────────────────────────────────

    @Query("SELECT SUM(c.coutTotal) FROM Commande c WHERE c.pointDeVente.id = :pdvId")
    Double sumCoutTotalByPointDeVenteId(@Param("pdvId") Long pdvId);

    @Query("SELECT SUM(c.coutTotal) FROM Commande c WHERE c.pointDeVente.id = :pdvId AND c.dateCommande BETWEEN :debut AND :fin")
    Double sumCoutTotalByPointDeVenteIdAndDateBetween(@Param("pdvId") Long pdvId,
                                                      @Param("debut") LocalDate debut,
                                                      @Param("fin") LocalDate fin);

    @Query("SELECT COUNT(c) FROM Commande c WHERE c.pointDeVente.id = :pdvId")
    long countByPointDeVenteId(@Param("pdvId") Long pdvId);

    @Query("SELECT c FROM Commande c WHERE c.pointDeVente.id = :pdvId ORDER BY c.dateCommande DESC")
    List<Commande> findByPointDeVenteId(@Param("pdvId") Long pdvId);
}
