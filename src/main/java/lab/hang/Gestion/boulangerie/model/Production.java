package lab.hang.Gestion.boulangerie.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Production {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate dateProduction;

    @ElementCollection
    @CollectionTable(name = "production_matiere_premiere", joinColumns = @JoinColumn(name = "production_id"))
    @MapKeyJoinColumn(name = "matiere_premiere_id")
    @Column(name = "quantite_utilisee")
    private Map<MatierePremiere, Double> matieresPremieresUtilisees = new HashMap<>();

    @ElementCollection
    @CollectionTable(name = "production_quantites_reelles", joinColumns = @JoinColumn(name = "production_id"))
    @MapKeyJoinColumn(name = "matiere_premiere_id")
    @Column(name = "quantite_reelle")
    private Map<MatierePremiere, Double> quantitesReellesUtilisees = new HashMap<>();

    @ElementCollection
    @CollectionTable(name = "production_produit", joinColumns = @JoinColumn(name = "production_id"))
    @MapKeyJoinColumn(name = "produit_id")
    @Column(name = "quantite_produite")
    private Map<Produit, Integer> produitsProduits = new HashMap<>();

    @ElementCollection
    @CollectionTable(name = "production_produits_restants", joinColumns = @JoinColumn(name = "production_id"))
    @MapKeyJoinColumn(name = "produit_id")
    @Column(name = "quantite_restante")
    private Map<Produit, Integer> produitsRestants = new HashMap<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private double quantiteCommandee;

    @OneToMany(mappedBy = "production")
    private Set<Commande> commandes = new HashSet<>();
}
