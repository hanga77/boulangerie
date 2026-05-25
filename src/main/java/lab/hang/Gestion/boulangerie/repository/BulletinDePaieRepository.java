package lab.hang.Gestion.boulangerie.repository;

import lab.hang.Gestion.boulangerie.model.BulletinDePaie;
import lab.hang.Gestion.boulangerie.model.Employe;
import lab.hang.Gestion.boulangerie.model.StatutBulletin;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BulletinDePaieRepository extends JpaRepository<BulletinDePaie, Long> {
    List<BulletinDePaie> findByEmploye(Employe employe);
    List<BulletinDePaie> findByPeriodeBetween(LocalDate debut, LocalDate fin);
    List<BulletinDePaie> findByStatut(StatutBulletin statut);
    Optional<BulletinDePaie> findByEmployeAndPeriode(Employe employe, LocalDate periode);
    List<BulletinDePaie> findByEmployeAndPeriodeBetween(Employe employe, LocalDate debut, LocalDate fin);
}
