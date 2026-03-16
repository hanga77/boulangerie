package lab.hang.Gestion.boulangerie.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "point_de_vente")
public class PointDeVente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    private String adresse;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypePointDeVente type;

    @Column(nullable = false)
    private boolean actif = true;

    @OneToMany(mappedBy = "pointDeVente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Guichet> guichets = new ArrayList<>();
}
