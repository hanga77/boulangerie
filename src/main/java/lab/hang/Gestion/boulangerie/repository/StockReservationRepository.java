package lab.hang.Gestion.boulangerie.repository;

import lab.hang.Gestion.boulangerie.model.StockReservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface StockReservationRepository extends JpaRepository<StockReservation, Long> {
    List<StockReservation> findByDateExpirationBefore(LocalDateTime now);
}
