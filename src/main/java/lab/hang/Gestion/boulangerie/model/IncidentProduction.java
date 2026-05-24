package lab.hang.Gestion.boulangerie.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter @Setter @NoArgsConstructor
public class IncidentProduction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "production_id")
    private Production production;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeIncident type;

    @ManyToOne(optional = true)
    @JoinColumn(name = "produit_id")
    private Produit produitConcerne;

    @ManyToOne(optional = true)
    @JoinColumn(name = "matiere_id")
    private MatierePremiere matiereConcernee;

    private double quantitePerdue;

    @Column(length = 500)
    private String cause;

    @Column(nullable = false)
    private LocalDate dateIncident;

    private boolean stockAjuste;

    @ManyToOne(optional = false)
    @JoinColumn(name = "signaled_by")
    private User signaledBy;

    @OneToOne(optional = true)
    private StockMovement mouvementStock;
}
