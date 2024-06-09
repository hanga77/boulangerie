package lab.hang.boulangerie.services.impl;

import lab.hang.boulangerie.dto.MouvementMatiereDTO;
import lab.hang.boulangerie.entity.MouvementMatiere;
import lab.hang.boulangerie.repository.MouvementMatiereRepository;
import lab.hang.boulangerie.services.MouvementMatiereServices;
import lab.hang.boulangerie.services.SessionServices;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class MouvementMaterielServicesImpl implements MouvementMatiereServices {
    private final MouvementMatiereRepository mouvementMatiereRepository;
    private final SessionServices sessionRepository;
    @Override
    public void SaveMouvMatiere(MouvementMatiereDTO mouvementMatiereDTO) {
        mouvementMatiereDTO.setSessionID(sessionRepository.getLastSessions());
        MouvementMatiere mouvementMatiere = mapToMouvementMatiere(mouvementMatiereDTO);
        mouvementMatiereRepository.save(mouvementMatiere);
    }

    @Override
    public List<MouvementMatiereDTO> getAllEntreOfDays(String action) {
        return mouvementMatiereRepository.findBySessionIDAndActions(sessionRepository.getLastSessions(),action).stream().map(this::mapToMouvementMatiereDTO).collect(Collectors.toList());
    }
    @Override
    public List<MouvementMatiereDTO> getAllDaysOfSessionAndActions(String action) {
        LocalDateTime debut = LocalDateTime.now();
        LocalDateTime fin = LocalDateTime.now().minusDays(1).withHour(0).withMinute(30);
        return mouvementMatiereRepository.findByDateCreatedBetweenAndActionsAndSessionID(debut,fin,action,sessionRepository.getLastSessions()).stream().map(this::mapToMouvementMatiereDTO).collect(Collectors.toList());
    }

    public MouvementMatiere mapToMouvementMatiere(MouvementMatiereDTO mouvementMatiereDTO){
        MouvementMatiere mouvementMatiere  = new MouvementMatiere();
        mouvementMatiere.setMatiereID(mouvementMatiereDTO.getMatiereID());
        mouvementMatiere.setID(mouvementMatiereDTO.getID());
        mouvementMatiere.setSessionID(mouvementMatiereDTO.getSessionID());
        mouvementMatiere.setActions(mouvementMatiereDTO.getActions());
        mouvementMatiere.setQte_stock(mouvementMatiereDTO.getQte_stock());
        mouvementMatiere.setUsersId(mouvementMatiereDTO.getUsersId());
        mouvementMatiere.setDateCreated(mouvementMatiereDTO.getDateCreated());

        return  mouvementMatiere;
    }

    public  MouvementMatiereDTO mapToMouvementMatiereDTO(MouvementMatiere mouvementMatiere){
        MouvementMatiereDTO mouvementMatiereDTO = new MouvementMatiereDTO();
        mouvementMatiereDTO.setActions(mouvementMatiere.getActions());
        mouvementMatiereDTO.setID(mouvementMatiere.getID());
        mouvementMatiereDTO.setDateCreated(mouvementMatiereDTO.getDateCreated());
        mouvementMatiereDTO.setUsersId(mouvementMatiere.getUsersId());
        mouvementMatiereDTO.setQte_stock(mouvementMatiere.getQte_stock());
        mouvementMatiereDTO.setMatiereID(mouvementMatiere.getMatiereID());
        mouvementMatiereDTO.setSessionID(mouvementMatiere.getSessionID());
        return mouvementMatiereDTO;
    }
}
