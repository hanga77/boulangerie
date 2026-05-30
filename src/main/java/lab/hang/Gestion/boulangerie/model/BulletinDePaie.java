package lab.hang.Gestion.boulangerie.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

@Entity
@Getter @Setter @NoArgsConstructor
@Table(name = "bulletin_de_paie",
       uniqueConstraints = @UniqueConstraint(columnNames = {"employe_id", "periode"}))
public class BulletinDePaie {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "employe_id")
    private Employe employe;

    @Column(nullable = false)
    private LocalDate periode;  // 1er du mois (MENSUEL) ou lundi de la semaine (HEBDOMADAIRE)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Periodicite periodicite = Periodicite.MENSUEL;

    private double salaireBase;
    private double primes;
    private double indemnitesTransport;
    private double salaireBrut;
    private double cnpsEmploye;     // 4,2 % du brut
    private double cnpsPatronal;    // 16,2 % du brut (informatif)
    private double avanceSurSalaire;
    private double salaireNet;      // brut - cnpsEmploye - avanceSurSalaire

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutBulletin statut;

    @Column(nullable = false)
    private LocalDate dateGeneration;

    private LocalDate datePaiement;

    @OneToOne(optional = true)
    private Transaction transaction;
}
