package lab.hang.boulangerie.dto;


import lab.hang.boulangerie.entity.Commande;
import lab.hang.boulangerie.entity.Products;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LigneCommandeDTO {
    private Long ID;

    private Products ProductList;

    private Commande commande;

    private int quantity;
}
