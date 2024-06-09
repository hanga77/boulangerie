package lab.hang.boulangerie.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "point_de_vente")
public class PointVente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pointVenteID;
    private String smallName;
    @OneToMany(targetEntity = Commande.class,mappedBy = "pointVenteID", cascade = CascadeType.ALL)
    private List<Commande> commandeList;
    @CreationTimestamp
    private LocalDateTime dateCreated;
}
