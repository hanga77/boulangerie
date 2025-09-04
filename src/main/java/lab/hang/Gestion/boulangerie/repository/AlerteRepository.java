package lab.hang.Gestion.boulangerie.repository;

import lab.hang.Gestion.boulangerie.model.Alerte;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlerteRepository  extends JpaRepository<Alerte, Long> {
    List<Alerte> findByVueFalseOrderByDateCreationDesc();

    List<Alerte> findAllByOrderByDateCreationDesc();

    List<Alerte> findByType(String type);

    List<Alerte> findByNiveau(String niveau);
}
