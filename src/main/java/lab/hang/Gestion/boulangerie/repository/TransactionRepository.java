package lab.hang.Gestion.boulangerie.repository;

import lab.hang.Gestion.boulangerie.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByDateBetween(LocalDate startDate, LocalDate endDate);

    List<Transaction> findByType(String production);

    List<Transaction> findByTypeIn(List<String> production);


}
