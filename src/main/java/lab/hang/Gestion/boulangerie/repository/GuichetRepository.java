package lab.hang.Gestion.boulangerie.repository;

import lab.hang.Gestion.boulangerie.model.Guichet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GuichetRepository extends JpaRepository<Guichet, Long> {

    List<Guichet> findByPointDeVenteId(Long pointDeVenteId);

    List<Guichet> findByPointDeVenteIdAndActifTrue(Long pointDeVenteId);

    @Query("SELECT g FROM Guichet g JOIN FETCH g.pointDeVente WHERE g.actif = true")
    List<Guichet> findByActifTrue();

    @Query("SELECT g FROM Guichet g JOIN FETCH g.pointDeVente WHERE g.id = :id")
    Optional<Guichet> findByIdWithPointDeVente(@Param("id") Long id);
}
