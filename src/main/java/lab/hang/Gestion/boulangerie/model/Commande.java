package lab.hang.Gestion.boulangerie.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "commande", indexes = {
        @Index(name = "idx_commande_date", columnList = "dateCommande"),
        @Index(name = "idx_commande_processed", columnList = "processed"),
        @Index(name = "idx_commande_date_processed", columnList = "dateCommande,processed")
})
public class Commande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate dateCommande;

    @ElementCollection
    @CollectionTable(name = "commande_produit", joinColumns = @JoinColumn(name = "commande_id"))
    @MapKeyJoinColumn(name = "produit_id")
    @Column(name = "quantite")
    private Map<Produit, Integer> produitsCommandes = new HashMap<>();

    @Column(nullable = false)
    private String pointDeVente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private Double coutTotal;

    @Column(nullable = false)
    private boolean processed = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "production_id")
    private Production production;
}
