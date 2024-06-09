package lab.hang.boulangerie.repository;

import lab.hang.boulangerie.entity.Commande;
import lab.hang.boulangerie.entity.LigneCommande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LigneCommandeRepository extends JpaRepository<LigneCommande, Long> {

    @Query("SELECT sum(lc.quantity) from LigneCommande  lc WHERE lc.commande=:commande and  lc.ProductList =:produits")
    int getSumQuantityForCommande(@Param("commande") Commande commande, @Param("produits") String produit);
}
