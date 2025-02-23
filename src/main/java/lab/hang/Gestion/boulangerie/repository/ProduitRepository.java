package lab.hang.Gestion.boulangerie.repository;


import lab.hang.Gestion.boulangerie.model.Produit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProduitRepository extends JpaRepository<Produit, Long> {
    List<Produit> findByNomContainingIgnoreCase(String nom); // Recherche par nom (insensible à la casse)

}
