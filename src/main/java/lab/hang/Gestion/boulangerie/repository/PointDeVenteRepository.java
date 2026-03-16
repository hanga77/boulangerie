package lab.hang.Gestion.boulangerie.repository;

import lab.hang.Gestion.boulangerie.model.PointDeVente;
import lab.hang.Gestion.boulangerie.model.TypePointDeVente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PointDeVenteRepository extends JpaRepository<PointDeVente, Long> {

    List<PointDeVente> findByActifTrue();

    List<PointDeVente> findByType(TypePointDeVente type);

    boolean existsByNom(String nom);

    @Query("SELECT p FROM PointDeVente p LEFT JOIN FETCH p.guichets WHERE p.id = :id")
    PointDeVente findByIdWithGuichets(@Param("id") Long id);
}
