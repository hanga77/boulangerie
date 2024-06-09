package lab.hang.boulangerie.dto;


import lab.hang.boulangerie.entity.Matiere;
import lab.hang.boulangerie.entity.StandardProduction;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LigneStandardDTO {
    private Long ID;
    private  StandardProduction standardProduction;
    private Matiere matiere;
    private  int quantity;
}
