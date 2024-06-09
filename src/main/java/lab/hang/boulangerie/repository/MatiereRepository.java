package lab.hang.boulangerie.repository;

import lab.hang.boulangerie.entity.Matiere;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatiereRepository extends JpaRepository<Matiere, Long> {

    Matiere findMatiereByNameMatiere(String matiere);
}
