package lab.hang.Gestion.boulangerie.domain.inventory;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Lot avec DLC propre à la matière (plus de +32 jours hardcodés).
 */
@Entity
@Table(name = "lot_v2", indexes = {
        @Index(name = "idx_lot_matiere", columnList = "matiere_premiere_id"),
        @Index(name = "idx_lot_dlc", columnList = "date_peremption")
})
@Getter
@Setter
@NoArgsConstructor
public class LotV2 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "matiere_premiere_id", nullable = false)
    private Long matierePremiereId;

    @Column(nullable = false)
    private double quantite;

    @Column(nullable = false)
    private double quantiteRestante;

    /** Prix d'entrée de ce lot (pour CMP / FIFO). */
    @Column(nullable = false)
    private double prixUnitaireEntree;

    @Column(name = "date_peremption")
    private LocalDate datePeremption;

    @Column(name = "date_entree", nullable = false)
    private LocalDate dateEntree = LocalDate.now();

    private Long fournisseurId;

    public LotV2(Long matierePremiereId, double quantite, double prixUnitaire,
                 LocalDate datePeremption, Long fournisseurId) {
        this.matierePremiereId = matierePremiereId;
        this.quantite = quantite;
        this.quantiteRestante = quantite;
        this.prixUnitaireEntree = prixUnitaire;
        this.datePeremption = datePeremption;
        this.fournisseurId = fournisseurId;
        this.dateEntree = LocalDate.now();
    }

    public void debiter(double qte) {
        if (qte > quantiteRestante) {
            throw new IllegalArgumentException("Lot insuffisant");
        }
        this.quantiteRestante -= qte;
    }

    public boolean estPerime(LocalDate ref) {
        return datePeremption != null && !datePeremption.isAfter(ref);
    }
}
