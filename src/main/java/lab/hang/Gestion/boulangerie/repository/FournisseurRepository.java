package lab.hang.Gestion.boulangerie.repository;

import lab.hang.Gestion.boulangerie.model.Fournisseur;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FournisseurRepository extends JpaRepository<Fournisseur, Long> {
    Fournisseur findByNom(String nom);
}