package lab.hang.Gestion.boulangerie.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "matiere_premiere", indexes = {
        @Index(name = "idx_matiere_nom", columnList = "nom")
})
public class MatierePremiere {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String uniteMesure;

    @Column(nullable = false)
    private double stock;

    @Column
    private double stockMinimum;

    @Column
    private double prixUnitaire = 0.0;

    public boolean isStockCritique() {
        return stock <= stockMinimum;
    }
}
