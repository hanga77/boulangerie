package lab.hang.Gestion.boulangerie.repository;

import lab.hang.Gestion.boulangerie.model.Lot;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface LotRepository extends JpaRepository<Lot, Long> {
    List<Lot> findByDatePeremptionBefore(LocalDate localDate);
}
