package lab.hang.Gestion.boulangerie.repository;

import lab.hang.Gestion.boulangerie.model.Versement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VersementRepository extends JpaRepository<Versement, Long> {

    List<Versement> findByPointDeVenteIdOrderByDateDesc(Long pointDeVenteId);

    @Query("SELECT COALESCE(SUM(v.montant), 0.0) FROM Versement v WHERE v.pointDeVente.id = :pointDeVenteId")
    double sumMontantByPointDeVenteId(@Param("pointDeVenteId") Long pointDeVenteId);
}
