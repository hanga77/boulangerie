package lab.hang.boulangerie.services;

import lab.hang.boulangerie.dto.MatiereDTO;
import lab.hang.boulangerie.entity.Matiere;

import java.util.List;

public interface MatiereServices {
    void SaveMatiere(MatiereDTO matiereDTO);
    void  DeleteMatiere(MatiereDTO matiereDTO);
    MatiereDTO UpdateMatiere(MatiereDTO matiereDTO);

    Matiere getMatiereByName(String matiere);

    List<MatiereDTO> getAllMatiere();

    MatiereDTO mapToMatiereDTO(Matiere matiere);

    Matiere mapToMatiere(MatiereDTO matiereDTO);
}
