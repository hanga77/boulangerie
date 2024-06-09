package lab.hang.boulangerie.services;

import lab.hang.boulangerie.dto.LigneStandardDTO;
import lab.hang.boulangerie.dto.ProductDTO;
import lab.hang.boulangerie.dto.StandardProductionDTO;
import lab.hang.boulangerie.entity.LigneStandard;
import lab.hang.boulangerie.entity.StandardProduction;

import java.util.List;

public interface StandardProductionServices {
    StandardProduction getProducts(ProductDTO productDTO);

    void SaveStandardProd(StandardProductionDTO standardProductionDTO, List<LigneStandardDTO> ligneStandardDTOS);
    StandardProductionDTO UpdateStandard(StandardProductionDTO standardProduction, List<LigneStandardDTO> ligneStandardDTOS);

    String DeleteStandard(StandardProductionDTO standardProductionDTO);

    List<StandardProductionDTO> getAllStandard();

    StandardProduction mapToStandardProduction(StandardProductionDTO standardProductionDTO);

    LigneStandard mapToLigneStandard(LigneStandardDTO ligneStandardDTO);

    StandardProductionDTO mapToStandardProductionDTO(StandardProduction standardProduction);
}
