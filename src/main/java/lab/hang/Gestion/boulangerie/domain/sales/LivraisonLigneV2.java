package lab.hang.Gestion.boulangerie.domain.sales;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "livraison_ligne_v2")
@Getter
@Setter
@NoArgsConstructor
public class LivraisonLigneV2 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "livraison_id", nullable = false)
    private LivraisonV2 livraison;

    @Column(name = "produit_id", nullable = false)
    private Long produitId;

    @Column(nullable = false)
    private int quantite;

    @Column(nullable = false)
    private double prixVente;

    public LivraisonLigneV2(Long produitId, int quantite, double prixVente) {
        this.produitId = produitId;
        this.quantite = quantite;
        this.prixVente = prixVente;
    }
}
