package lab.hang.boulangerie.services.impl;

import lab.hang.boulangerie.dto.PointVenteDTO;
import lab.hang.boulangerie.entity.PointVente;
import lab.hang.boulangerie.repository.PointDeVenteRepository;
import lab.hang.boulangerie.services.PointDeVenteServices;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PointDeVenteServicesImpl implements PointDeVenteServices {

    private final PointDeVenteRepository pointDeVenteRepository;
    @Override
    public void SavePointDeVente(PointVente pointVente) {

    }

    @Override
    public String DeletePointeDeVente(Long pointVente) {
        pointDeVenteRepository.deleteById(pointVente);
        return "Delete Success";
    }

    @Override
    public PointVenteDTO UpdatePointVente(PointVente pointVente) {
        PointVente pointVente1 = pointDeVenteRepository.findById(pointVente.getPointVenteID()).orElseThrow();
        pointVente1.setSmallName(pointVente.getSmallName());
        return null;
    }

    @Override
    public List<PointVenteDTO> getAllPointVente() {
        return pointDeVenteRepository.findAll(Sort.by(Sort.Direction.DESC,"dateCreated")).stream().map(this::mapToPointVenteDTO).collect(Collectors.toList());
    }

    @Override
    public PointVenteDTO getPointVente(PointVente pointVente) {
        return pointDeVenteRepository.findPointVenteBySmallName(pointVente.getSmallName()).map(this::mapToPointVenteDTO).orElseThrow();
    }

    private PointVenteDTO mapToPointVenteDTO(PointVente pointVente){
        PointVenteDTO pointVenteDTO = new PointVenteDTO();
        pointVenteDTO.setPointVenteID(pointVente.getPointVenteID());
        pointVenteDTO.setSmallName(pointVente.getSmallName());
        pointVenteDTO.setCommandeList(pointVente.getCommandeList());
        pointVenteDTO.setDateCreated(pointVente.getDateCreated());
        return pointVenteDTO;
    }
}
