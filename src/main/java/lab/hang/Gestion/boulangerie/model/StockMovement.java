package lab.hang.Gestion.boulangerie.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "stock_movement", indexes = {
        @Index(name = "idx_stock_movement_date", columnList = "date"),
        @Index(name = "idx_stock_movement_type", columnList = "type")
})
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Pattern(regexp = "^(ENTREE|SORTIE)$")
    private String type;

    @Column(nullable = false)
    @Positive
    private double quantite;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matiere_premiere_id", nullable = false)
    private MatierePremiere matierePremiere;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    private String motif;
}
