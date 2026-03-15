package lab.hang.Gestion.boulangerie.repository;

import lab.hang.Gestion.boulangerie.model.EcritureComptable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface EcritureComptableRepository extends JpaRepository<EcritureComptable, String> {

    List<EcritureComptable> findByDateBetweenOrderByDateAsc(LocalDate debut, LocalDate fin);

    List<EcritureComptable> findByCompteAndDateBetweenOrderByDateAsc(String compte, LocalDate debut, LocalDate fin);

    @Query("SELECT e FROM EcritureComptable e WHERE e.date BETWEEN :debut AND :fin AND e.compte LIKE :prefix%")
    List<EcritureComptable> findByDateBetweenAndCompteStartingWith(
            @Param("debut") LocalDate debut,
            @Param("fin") LocalDate fin,
            @Param("prefix") String prefix);

    @Query("SELECT COALESCE(SUM(e.debit), 0) FROM EcritureComptable e WHERE e.compte LIKE :prefix% AND e.date BETWEEN :debut AND :fin")
    double sumDebitByComptePrefix(@Param("prefix") String prefix, @Param("debut") LocalDate debut, @Param("fin") LocalDate fin);

    @Query("SELECT COALESCE(SUM(e.credit), 0) FROM EcritureComptable e WHERE e.compte LIKE :prefix% AND e.date BETWEEN :debut AND :fin")
    double sumCreditByComptePrefix(@Param("prefix") String prefix, @Param("debut") LocalDate debut, @Param("fin") LocalDate fin);
}
