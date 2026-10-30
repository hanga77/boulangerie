package lab.hang.Gestion.boulangerie.domain.production;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Agrégat Production refondu — remplace l'entité Production basée sur @ElementCollection.
 */
@Entity
@Table(name = "production_v2", indexes = {
        @Index(name = "idx_prod_v2_date", columnList = "date_production"),
        @Index(name = "idx_prod_v2_statut", columnList = "statut")
})
@Getter
@Setter
@NoArgsConstructor
public class ProductionV2 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_production", nullable = false)
    private LocalDate dateProduction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductionStatus statut = ProductionStatus.PLANIFIEE;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @OneToMany(mappedBy = "production", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductionLigneProduit> lignesProduits = new ArrayList<>();

    @OneToMany(mappedBy = "production", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductionLigneMatiere> lignesMatieres = new ArrayList<>();

    public ProductionV2(LocalDate dateProduction, Long userId) {
        this.dateProduction = dateProduction;
        this.userId = userId;
        this.statut = ProductionStatus.EN_COURS;
    }

    public void ajouterLigneProduit(Long produitId, int quantitePlanifiee) {
        ProductionLigneProduit ligne = new ProductionLigneProduit(produitId, quantitePlanifiee);
        ligne.setProduction(this);
        lignesProduits.add(ligne);
    }

    public void ajouterOuCumulerMatiere(Long matiereId, double quantiteTheorique) {
        Optional<ProductionLigneMatiere> existing = lignesMatieres.stream()
                .filter(l -> l.getMatierePremiereId().equals(matiereId))
                .findFirst();
        if (existing.isPresent()) {
            existing.get().setQuantiteTheorique(
                    existing.get().getQuantiteTheorique() + quantiteTheorique);
        } else {
            ProductionLigneMatiere ligne = new ProductionLigneMatiere(matiereId, quantiteTheorique);
            ligne.setProduction(this);
            lignesMatieres.add(ligne);
        }
    }

    public void valider() {
        if (statut != ProductionStatus.EN_COURS) {
            throw new IllegalStateException("Seule une production EN_COURS peut être validée");
        }
        this.statut = ProductionStatus.VALIDEE;
    }

    public void cloturer() {
        if (statut != ProductionStatus.VALIDEE) {
            throw new IllegalStateException("Seule une production VALIDEE peut être clôturée");
        }
        this.statut = ProductionStatus.CLOTUREE;
    }

    public Optional<ProductionLigneProduit> ligneProduit(Long produitId) {
        return lignesProduits.stream()
                .filter(l -> l.getProduitId().equals(produitId))
                .findFirst();
    }

    public Optional<ProductionLigneMatiere> ligneMatiere(Long matiereId) {
        return lignesMatieres.stream()
                .filter(l -> l.getMatierePremiereId().equals(matiereId))
                .findFirst();
    }
}
