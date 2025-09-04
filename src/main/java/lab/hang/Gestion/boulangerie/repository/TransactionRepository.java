package lab.hang.Gestion.boulangerie.repository;

import lab.hang.Gestion.boulangerie.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByDateBetween(LocalDate startDate, LocalDate endDate);

    List<Transaction> findByType(String type);

    @Query("SELECT COALESCE(SUM(t.montant), 0.0) FROM Transaction t WHERE t.type = :type")
    Double sumMontantByType(@Param("type") String type);

    @Query("SELECT COALESCE(SUM(t.montant), 0.0) FROM Transaction t WHERE t.type IN :types")
    Double sumMontantByTypeIn(@Param("types") List<String> types);

    @Query("SELECT t FROM Transaction t WHERE t.type = :type AND t.date BETWEEN :startDate AND :endDate")
    List<Transaction> findByDateBetweenAndType(@Param("type") String type, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}