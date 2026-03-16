package lab.hang.Gestion.boulangerie.repository;

import lab.hang.Gestion.boulangerie.model.Guichet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GuichetRepository extends JpaRepository<Guichet, Long> {

    List<Guichet> findByPointDeVenteId(Long pointDeVenteId);

    List<Guichet> findByPointDeVenteIdAndActifTrue(Long pointDeVenteId);

    List<Guichet> findByActifTrue();
}
