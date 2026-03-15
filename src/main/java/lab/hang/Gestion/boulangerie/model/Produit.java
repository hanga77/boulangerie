package lab.hang.Gestion.boulangerie.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "produit", indexes = {
        @Index(name = "idx_produit_nom", columnList = "nom")
})
public class Produit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private double prix;

    @Column(nullable = false)
    private double quantiteAttendue;

    @ElementCollection
    @CollectionTable(name = "produit_matiere_premiere", joinColumns = @JoinColumn(name = "produit_id"))
    @MapKeyJoinColumn(name = "matiere_premiere_id")
    @Column(name = "quantite")
    private Map<MatierePremiere, Double> matieresPremieres;
}
