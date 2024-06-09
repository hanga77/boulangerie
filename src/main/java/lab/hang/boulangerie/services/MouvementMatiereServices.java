package lab.hang.boulangerie.services;

import lab.hang.boulangerie.dto.MouvementMatiereDTO;
import lab.hang.boulangerie.entity.MouvementMatiere;
import lab.hang.boulangerie.entity.Session;

import java.time.LocalDateTime;
import java.util.List;

public interface MouvementMatiereServices {
    void SaveMouvMatiere(MouvementMatiereDTO mouvementMatiereDTO);
    List<MouvementMatiereDTO> getAllEntreOfDays(String action);

    List<MouvementMatiereDTO> getAllDaysOfSessionAndActions(String action);
}
