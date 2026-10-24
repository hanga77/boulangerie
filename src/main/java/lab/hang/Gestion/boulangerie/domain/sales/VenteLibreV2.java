package lab.hang.Gestion.boulangerie.domain.sales;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "vente_libre_v2", indexes = {
        @Index(name = "idx_vente_v2_prod", columnList = "production_v2_id"),
        @Index(name = "idx_vente_v2_date", columnList = "date_vente")
})
@Getter
@Setter
@NoArgsConstructor
public class VenteLibreV2 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_vente", nullable = false)
    private LocalDate dateVente = LocalDate.now();

    @Column(name = "production_v2_id", nullable = false)
    private Long productionV2Id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "guichet_id")
    private Long guichetId;

    /** ESPECES | MOBILE_MONEY */
    @Column(length = 20)
    private String moyenPaiement = "ESPECES";

    private double montantTotal;

    @OneToMany(mappedBy = "vente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VenteLigneV2> lignes = new ArrayList<>();

    public void ajouterLigne(Long produitId, int quantite, double prixUnitaire) {
        VenteLigneV2 ligne = new VenteLigneV2(produitId, quantite, prixUnitaire);
        ligne.setVente(this);
        lignes.add(ligne);
        montantTotal += quantite * prixUnitaire;
    }
}
