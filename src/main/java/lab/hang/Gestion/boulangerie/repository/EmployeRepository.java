package lab.hang.Gestion.boulangerie.repository;

import lab.hang.Gestion.boulangerie.model.Employe;
import lab.hang.Gestion.boulangerie.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface EmployeRepository extends JpaRepository<Employe, Long> {
    List<Employe> findByActifTrue();
    Optional<Employe> findByUser(User user);
}
