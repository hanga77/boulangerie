package lab.hang.Gestion.boulangerie.repository;

import lab.hang.Gestion.boulangerie.model.Facture;
import lab.hang.Gestion.boulangerie.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public interface FactureRepository extends JpaRepository<Facture, Long> {

    List<Facture> findByStatut(String statut);
    List<Facture> findByClientAndStatut(User client, String statut);
    List<Facture> findByDateEcheanceBeforeAndStatut(LocalDate date, String statut);

    List<Facture> findByStatutNot(String payee);
}
