package lab.hang.boulangerie.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "ligne_standard")
public class LigneStandard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ID;
    @ManyToOne(cascade = CascadeType.ALL,targetEntity = StandardProduction.class)
    @JoinColumn(name = "standard_id")
    private  StandardProduction standardProduction;
    @ManyToOne(cascade = CascadeType.ALL,targetEntity = Matiere.class)
    @JoinColumn(name = "matiere_id")
    private Matiere matiere;
    private  int quantity;
}
