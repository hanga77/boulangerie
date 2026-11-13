package lab.hang.Gestion.boulangerie.repository;

import lab.hang.Gestion.boulangerie.model.PilotObservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface PilotObservationRepository extends JpaRepository<PilotObservation, Long> {

    List<PilotObservation> findByDateJourOrderByCreatedAtDesc(LocalDate dateJour);

    List<PilotObservation> findAllByOrderByCreatedAtDesc();
}
