package lab.hang.Gestion.boulangerie.repository;

import jakarta.persistence.LockModeType;
import lab.hang.Gestion.boulangerie.model.MatierePremiere;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MatierePremiereRepository extends JpaRepository<MatierePremiere, Long> {
    List<MatierePremiere> findByNomContainingIgnoreCase(String nom);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM MatierePremiere m WHERE m.id = :id")
    Optional<MatierePremiere> findByIdWithLock(@Param("id") Long id);
}
