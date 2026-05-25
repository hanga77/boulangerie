package lab.hang.Gestion.boulangerie.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

@Entity
@Getter @Setter @NoArgsConstructor
@Table(name = "employe")
public class Employe {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Column(nullable = false)
    private String poste;

    private double salaireBase;
    private LocalDate dateEmbauche;
    private String numeroCnps;
    private boolean actif = true;

    @ManyToOne(optional = true)
    @JoinColumn(name = "user_id")
    private User user;
}
