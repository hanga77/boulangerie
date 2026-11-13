package lab.hang.Gestion.boulangerie.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Phase 7 — observation saisie pendant le pilote terrain.
 */
@Entity
@Table(name = "pilot_observation", indexes = {
        @Index(name = "idx_pilot_obs_date", columnList = "date_jour"),
        @Index(name = "idx_pilot_obs_etape", columnList = "etape")
})
@Getter
@Setter
@NoArgsConstructor
public class PilotObservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_jour", nullable = false)
    private LocalDate dateJour;

    @Column(nullable = false, length = 40)
    private String etape;

    @Column(nullable = false)
    private boolean ok = true;

    @Column(length = 2000)
    private String commentaire;

    @Column(length = 80)
    private String auteur;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (dateJour == null) {
            dateJour = LocalDate.now();
        }
    }
}
