package lab.hang.boulangerie.repository;

import lab.hang.boulangerie.entity.Commande;
import lab.hang.boulangerie.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CommandeRepository  extends JpaRepository<Commande, Long> {

    List<Commande> findCommandeBySession(Session session);

    Optional<Commande> findByCommandeID(Long aLong);
    List<Commande> findByDateCreatedBetweenAndSession(LocalDateTime debut, LocalDateTime fin, Session session);

}
