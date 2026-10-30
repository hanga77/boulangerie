package lab.hang.Gestion.boulangerie.domain.production;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Remplace Map&lt;MatierePremiere, Double&gt; matieresPremieresUtilisees / quantitesReellesUtilisees.
 */
@Entity
@Table(name = "production_ligne_matiere", indexes = {
        @Index(name = "idx_plm_production", columnList = "production_id"),
        @Index(name = "idx_plm_matiere", columnList = "matiere_premiere_id")
})
@Getter
@Setter
@NoArgsConstructor
public class ProductionLigneMatiere {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "production_id", nullable = false)
    private ProductionV2 production;

    @Column(name = "matiere_premiere_id", nullable = false)
    private Long matierePremiereId;

    /** Quantité théorique calculée depuis les recettes. */
    @Column(nullable = false)
    private double quantiteTheorique;

    /** Quantité réellement sortie par le magasinier (nullable tant que non confirmée). */
    private Double quantiteMagasinier;

    /** Quantité déclarée utilisée par le boulanger. */
    private Double quantiteBoulanger;

    public ProductionLigneMatiere(Long matierePremiereId, double quantiteTheorique) {
        this.matierePremiereId = matierePremiereId;
        this.quantiteTheorique = quantiteTheorique;
    }

    public void confirmerMagasinier(double quantite) {
        if (quantite < 0) {
            throw new IllegalArgumentException("Quantité magasinier négative");
        }
        this.quantiteMagasinier = quantite;
    }

    public void declarerBoulanger(double quantite) {
        if (quantite < 0) {
            throw new IllegalArgumentException("Quantité boulanger négative");
        }
        this.quantiteBoulanger = quantite;
    }

    public double ecartPourcent() {
        Double reel = quantiteMagasinier != null ? quantiteMagasinier : quantiteBoulanger;
        if (reel == null || quantiteTheorique == 0) {
            return 0;
        }
        return Math.abs(reel - quantiteTheorique) / quantiteTheorique * 100.0;
    }
}
