package lab.hang.Gestion.boulangerie.repository;

import lab.hang.Gestion.boulangerie.model.Reclamation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReclamationRepository extends JpaRepository<Reclamation, Long> {

    List<Reclamation> findByPointDeVenteIdOrderByDateCreationDesc(Long pointDeVenteId);

    List<Reclamation> findAllByOrderByDateCreationDesc();

    long countByStatut(String statut);
}
