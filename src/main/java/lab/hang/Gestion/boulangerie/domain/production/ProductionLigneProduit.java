package lab.hang.Gestion.boulangerie.domain.production;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Remplace Map&lt;Produit, Integer&gt; produitsProduits / produitsRestants.
 */
@Entity
@Table(name = "production_ligne_produit", indexes = {
        @Index(name = "idx_plp_production", columnList = "production_id"),
        @Index(name = "idx_plp_produit", columnList = "produit_id")
})
@Getter
@Setter
@NoArgsConstructor
public class ProductionLigneProduit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "production_id", nullable = false)
    private ProductionV2 production;

    @Column(name = "produit_id", nullable = false)
    private Long produitId;

    @Column(nullable = false)
    private int quantitePlanifiee;

    @Column(nullable = false)
    private int quantiteReelle;

    @Column(nullable = false)
    private int quantiteRestante;

    public ProductionLigneProduit(Long produitId, int quantitePlanifiee) {
        this.produitId = produitId;
        this.quantitePlanifiee = quantitePlanifiee;
        this.quantiteReelle = quantitePlanifiee;
        this.quantiteRestante = quantitePlanifiee;
    }

    public void validerQuantiteReelle(int reelle) {
        if (reelle < 0) {
            throw new IllegalArgumentException("Quantité réelle négative pour produit " + produitId);
        }
        this.quantiteReelle = reelle;
        this.quantiteRestante = reelle;
    }

    public void debiterRestant(int quantite) {
        if (quantite > quantiteRestante) {
            throw new IllegalArgumentException(
                    "Quantité demandée (" + quantite + ") > restante (" + quantiteRestante +
                            ") pour produit " + produitId);
        }
        this.quantiteRestante -= quantite;
    }
}
