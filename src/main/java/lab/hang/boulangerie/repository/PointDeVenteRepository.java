package lab.hang.boulangerie.repository;

import lab.hang.boulangerie.entity.PointVente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PointDeVenteRepository  extends JpaRepository<PointVente, Long> {

    Optional<PointVente> findPointVenteBySmallName(String mall);
}
