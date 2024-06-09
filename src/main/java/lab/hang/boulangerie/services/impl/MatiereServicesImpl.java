package lab.hang.boulangerie.services.impl;

import lab.hang.boulangerie.dto.MatiereDTO;
import lab.hang.boulangerie.entity.Matiere;
import lab.hang.boulangerie.repository.MatiereRepository;
import lab.hang.boulangerie.services.MatiereServices;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
@AllArgsConstructor
public class MatiereServicesImpl implements MatiereServices {
    private final MatiereRepository matiereRepository;
    @Override
    public void SaveMatiere(MatiereDTO matiereDTO) {
        matiereRepository.save(mapToMatiere(matiereDTO));
    }

    @Override
    public void DeleteMatiere(MatiereDTO matiereDTO) {
        matiereRepository.deleteById(matiereDTO.getMatiereID());
    }

    @Override
    public MatiereDTO UpdateMatiere(MatiereDTO matiereDTO) {
        Matiere matiere = matiereRepository.findById(matiereDTO.getMatiereID()).orElseThrow();
        matiere.setNameMatiere(matiereDTO.getNameMatiere());
        matiereRepository.save(matiere);
        return mapToMatiereDTO(matiere);
    }

    @Override
    public Matiere getMatiereByName(String  matiere){
        return matiereRepository.findMatiereByNameMatiere(matiere);
    }

    @Override
    public List<MatiereDTO> getAllMatiere() {
        return matiereRepository.findAll().stream().map(this::mapToMatiereDTO).collect(Collectors.toList());
    }

    @Override
    public MatiereDTO mapToMatiereDTO(Matiere matiere){
        MatiereDTO matiereDTO = new MatiereDTO();
        matiereDTO.setMatiereID(matiereDTO.getMatiereID());
        matiereDTO.setNameMatiere(matiere.getNameMatiere());
        return  matiereDTO;
    }

    @Override
    public Matiere mapToMatiere(MatiereDTO matiereDTO){
        Matiere matiere = new Matiere();
        matiere.setMatiereID(matiereDTO.getMatiereID());
        matiere.setNameMatiere(matiereDTO.getNameMatiere());
        return matiere;
    }
}
