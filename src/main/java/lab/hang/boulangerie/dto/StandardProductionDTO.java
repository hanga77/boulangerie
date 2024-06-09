package lab.hang.boulangerie.dto;

import lab.hang.boulangerie.entity.LigneStandard;
import lab.hang.boulangerie.entity.Products;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class StandardProductionDTO {
    private  Long IDStandard;

    private Products products;

    private List<LigneStandard> ligneStandards;
}
