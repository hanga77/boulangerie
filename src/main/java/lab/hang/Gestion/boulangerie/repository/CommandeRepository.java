package lab.hang.Gestion.boulangerie.repository;

import lab.hang.Gestion.boulangerie.model.Commande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public interface CommandeRepository extends JpaRepository<Commande, Long> {

    // Récupérer toutes les commandes pour une date donnée
    List<Commande> findByDateCommande(LocalDate date);

    // Compter le nombre de commandes pour une date donnée
    @Query("SELECT COUNT(c) FROM Commande c WHERE c.dateCommande = :date")
    int countByDateCommande(@Param("date") LocalDate date);

    // Récupérer le coût total des commandes pour une date donnée
    @Query("SELECT SUM(c.coutTotal) FROM Commande c WHERE c.dateCommande = :date")
    Double sumCoutTotalByDateCommande(@Param("date") LocalDate date);

    // Récupérer le nombre de points de vente actifs pour une date donnée
    @Query("SELECT COUNT(DISTINCT c.pointDeVente) FROM Commande c WHERE c.dateCommande = :date")
    int countDistinctPointsDeVenteByDateCommande(@Param("date") LocalDate date);


    boolean existsByDateCommande(LocalDate date);

    List<Commande> findByProcessedFalse();

    List<Commande> findByDateCommandeAndProcessed(LocalDate date, boolean processed);


    List<Commande> findByProductionId(Long productionId);

    Long countDistinctPointDeVenteByDateCommande(LocalDate date);
}