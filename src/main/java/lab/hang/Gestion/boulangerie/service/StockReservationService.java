package lab.hang.Gestion.boulangerie.service;

import lab.hang.Gestion.boulangerie.model.StockReservation;
import lab.hang.Gestion.boulangerie.repository.StockReservationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class StockReservationService {

    private final StockReservationRepository stockReservationRepository;

    public StockReservationService(StockReservationRepository stockReservationRepository) {
        this.stockReservationRepository = stockReservationRepository;
    }

    public StockReservation reserveStock(StockReservation reservation) {
        return stockReservationRepository.save(reservation);
    }

    public void cancelReservation(Long reservationId) {
        stockReservationRepository.deleteById(reservationId);
    }

    public List<StockReservation> getExpiredReservations() {
        return stockReservationRepository.findByDateExpirationBefore(LocalDateTime.now());
    }
}