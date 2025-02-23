package lab.hang.Gestion.boulangerie.repository;

import lab.hang.Gestion.boulangerie.model.CompteBancaire;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompteBancaireRepository extends JpaRepository<CompteBancaire, Long> {
    CompteBancaire findByNom(String comptePrincipal);
}
