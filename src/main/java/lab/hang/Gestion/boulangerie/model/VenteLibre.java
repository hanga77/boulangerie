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
public class VenteLibre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate dateVente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "production_id", nullable = false)
    private Production production;

    @ElementCollection
    @CollectionTable(name = "vente_libre_produit", joinColumns = @JoinColumn(name = "vente_libre_id"))
    @MapKeyJoinColumn(name = "produit_id")
    @Column(name = "quantite_vendue")
    private Map<Produit, Integer> produitsVendus = new HashMap<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private double montantTotal;
}
