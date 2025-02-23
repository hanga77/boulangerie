package lab.hang.Gestion.boulangerie.repository;

import lab.hang.Gestion.boulangerie.model.FournisseurDette;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FournisseurDetteRepository extends JpaRepository<FournisseurDette, Long> {
    List<FournisseurDette> findByFournisseurId(Long fournisseurId);
    List<FournisseurDette> findByStatus(String status);
}