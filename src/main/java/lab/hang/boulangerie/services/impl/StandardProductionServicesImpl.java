package lab.hang.boulangerie.services.impl;

import lab.hang.boulangerie.dto.LigneStandardDTO;
import lab.hang.boulangerie.dto.ProductDTO;
import lab.hang.boulangerie.dto.StandardProductionDTO;
import lab.hang.boulangerie.entity.LigneStandard;
import lab.hang.boulangerie.entity.StandardProduction;
import lab.hang.boulangerie.repository.LigneStandardRepository;
import lab.hang.boulangerie.repository.StandardProductionRepository;
import lab.hang.boulangerie.services.StandardProductionServices;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class StandardProductionServicesImpl implements StandardProductionServices {
    private final StandardProductionRepository standardProductionRepository;
    private final LigneStandardRepository ligneStandardRepository;

    @Override
    public  StandardProduction getProducts(ProductDTO productDTO){
        return standardProductionRepository.findByProducts(productDTO);
    }
    @Override
    public void SaveStandardProd(StandardProductionDTO standardProductionDTO, List<LigneStandardDTO> ligneStandard) {
        StandardProduction standardProduction = mapToStandardProduction(standardProductionDTO);
        standardProductionRepository.save(standardProduction);
        for (LigneStandardDTO ligneStandardDTO : ligneStandard){
            ligneStandardRepository.save(mapToLigneStandard(ligneStandardDTO));
        }
    }

    @Override
    public StandardProductionDTO UpdateStandard(StandardProductionDTO standardProductionDTO, List<LigneStandardDTO> ligneStandardDTOS) {
        StandardProduction standardProduction = standardProductionRepository.findById(standardProductionDTO.getIDStandard()).orElseThrow();
        for (LigneStandard ligneStandard : standardProduction.getLigneStandards()){
            int index = standardProduction.getLigneStandards().indexOf(ligneStandard);
                ligneStandard.setMatiere(ligneStandardDTOS.get(index).getMatiere());
                ligneStandard.setQuantity(ligneStandardDTOS.get(index).getQuantity());
                ligneStandardRepository.saveAndFlush(ligneStandard);
        }

        return mapToStandardProductionDTO(standardProduction);
    }
    @Override
    public String DeleteStandard(StandardProductionDTO standardProductionDTO) {
        standardProductionRepository.deleteById(standardProductionDTO.getIDStandard());
        return "Delete Success";
    }

    @Override
    public List<StandardProductionDTO> getAllStandard() {
        return standardProductionRepository.findAll(Sort.by(Sort.Direction.DESC,"dateCreated")).stream().map(this::mapToStandardProductionDTO).collect(Collectors.toList());
    }

    @Override
    public StandardProduction mapToStandardProduction(StandardProductionDTO standardProductionDTO){
        StandardProduction standardProduction = new StandardProduction();
        standardProduction.setIDStandard(standardProductionDTO.getIDStandard());
        standardProduction.setProducts(standardProductionDTO.getProducts());
        standardProduction.setLigneStandards(standardProductionDTO.getLigneStandards());
        return standardProduction;
    }

    @Override
    public LigneStandard mapToLigneStandard(LigneStandardDTO ligneStandardDTO){
        LigneStandard ligneStandard = new LigneStandard();
        ligneStandard.setID(ligneStandardDTO.getID());
        ligneStandard.setStandardProduction(ligneStandardDTO.getStandardProduction());
        ligneStandard.setQuantity(ligneStandardDTO.getQuantity());
        ligneStandard.setMatiere(ligneStandardDTO.getMatiere());
        return ligneStandard;
    }

    @Override
    public StandardProductionDTO mapToStandardProductionDTO(StandardProduction standardProduction){
        StandardProductionDTO standardProductionDTO = new StandardProductionDTO();
        standardProductionDTO.setIDStandard(standardProduction.getIDStandard());
        standardProductionDTO.setProducts(standardProduction.getProducts());
        standardProductionDTO.setLigneStandards(standardProductionDTO.getLigneStandards());
        return standardProductionDTO;
    }
}
