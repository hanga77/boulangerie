package lab.hang.Gestion.boulangerie.domain.sales;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "livraison_v2", indexes = {
        @Index(name = "idx_liv_v2_prod", columnList = "production_v2_id"),
        @Index(name = "idx_liv_v2_date", columnList = "date_livraison")
})
@Getter
@Setter
@NoArgsConstructor
public class LivraisonV2 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_livraison", nullable = false)
    private LocalDate dateLivraison = LocalDate.now();

    @Column(nullable = false)
    private String nomClient;

    @Column(name = "point_de_vente_id")
    private Long pointDeVenteId;

    @Column(name = "production_v2_id", nullable = false)
    private Long productionV2Id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    private double montantTotal;

    @Column(nullable = false)
    private boolean revenuEnregistre = false;

    /** Lien vers la facture émise (phase 3 P2). */
    @Column(name = "facture_id")
    private Long factureId;

    @Column(name = "facture_numero", length = 64)
    private String factureNumero;

    @OneToMany(mappedBy = "livraison", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LivraisonLigneV2> lignes = new ArrayList<>();

    public void ajouterLigne(Long produitId, int quantite, double prixVente) {
        LivraisonLigneV2 ligne = new LivraisonLigneV2(produitId, quantite, prixVente);
        ligne.setLivraison(this);
        lignes.add(ligne);
        montantTotal += quantite * prixVente;
    }
}
