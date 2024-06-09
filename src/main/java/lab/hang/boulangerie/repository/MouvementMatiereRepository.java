package lab.hang.boulangerie.repository;


import lab.hang.boulangerie.entity.MouvementMatiere;
import lab.hang.boulangerie.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface MouvementMatiereRepository extends JpaRepository<MouvementMatiere,Long> {

    List<MouvementMatiere> findBySessionID(Session sessionID);
    List<MouvementMatiere> findBySessionIDAndActions(Session session, String action);
    List<MouvementMatiere> findByDateCreatedBetweenAndActionsAndSessionID(LocalDateTime debut, LocalDateTime fin, String action,Session session);


}
