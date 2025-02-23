package lab.hang.Gestion.boulangerie.repository;

import lab.hang.Gestion.boulangerie.model.MatierePremiere;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatierePremiereRepository extends JpaRepository<MatierePremiere, Long> {
    List<MatierePremiere> findByNomContainingIgnoreCase(String nom); // Recherche par nom (insensible à la casse)
}
