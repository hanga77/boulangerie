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
@Table(name = "ligne_commande")
public class LigneCommande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ID;
    @ManyToOne(cascade= CascadeType.ALL)
    @JoinColumn(name = "product_Id")
    private Products ProductList;

    @ManyToOne(cascade= CascadeType.ALL)
    @JoinColumn(name = "commande_id")
    private Commande commande;

    private int quantity;
}
