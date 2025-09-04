package lab.hang.Gestion.boulangerie.repository;

import lab.hang.Gestion.boulangerie.model.ChargeFixe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public interface ChargeFixeRepository extends JpaRepository<ChargeFixe, Long> {

    List<ChargeFixe> findByDateEcheanceBetween(LocalDate debut, LocalDate fin);
    List<ChargeFixe> findByTypeAndPaye(String type, boolean paye);
    int countByPayeAndDateEcheanceBefore(boolean paye, LocalDate date);

    Arrays findByPayeFalseAndDateEcheanceBefore(LocalDate today);

    Arrays findByPayeFalseAndDateEcheanceBetween(LocalDate today, LocalDate localDate);

    Arrays findByPayeTrue();
}
