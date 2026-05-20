package lab.hang.Gestion.boulangerie.repository;

import lab.hang.Gestion.boulangerie.model.CompteBancaire;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompteBancaireRepository extends JpaRepository<CompteBancaire, Long> {
    Optional<CompteBancaire> findByNom(String nom);
}
