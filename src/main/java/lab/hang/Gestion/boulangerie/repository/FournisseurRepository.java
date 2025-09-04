package lab.hang.Gestion.boulangerie.repository;

import lab.hang.Gestion.boulangerie.model.Fournisseur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FournisseurRepository extends JpaRepository<Fournisseur, Long> {
    Fournisseur findByNom(String nom);

    List<Fournisseur> findByNomContainingIgnoreCase(String nom);
}