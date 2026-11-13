package lab.hang.Gestion.boulangerie.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "stock_movement", indexes = {
        @Index(name = "idx_stock_movement_date", columnList = "date"),
        @Index(name = "idx_stock_movement_type", columnList = "type"),
        @Index(name = "idx_stock_movement_prod_v2", columnList = "production_v2_id")
})
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Pattern(regexp = "^(ENTREE|SORTIE|RETOUR|PERTE)$")
    private String type;

    @Column(nullable = false)
    @Positive
    private double quantite;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matiere_premiere_id", nullable = false)
    private MatierePremiere matierePremiere;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    private String motif;

    /** Quantité commandée au fournisseur (ENTRÉE uniquement). */
    private Double quantiteCommandee;

    /** Quantité avariée à la réception (ENTRÉE uniquement). */
    private Double quantiteAvariee;

    /** Lien production V1 (historique). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "production_id")
    private Production production;

    /** Lien production V2 (phase 4) — sorties cycle journalier. */
    @Column(name = "production_v2_id")
    private Long productionV2Id;
}
