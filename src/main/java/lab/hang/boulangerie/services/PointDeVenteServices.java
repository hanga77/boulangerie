package lab.hang.boulangerie.services;

import lab.hang.boulangerie.dto.PointVenteDTO;
import lab.hang.boulangerie.entity.PointVente;

import java.util.List;

public interface PointDeVenteServices {
    void SavePointDeVente(PointVente pointVente);
    String DeletePointeDeVente(Long pointVente);
    PointVenteDTO UpdatePointVente(PointVente pointVente);
    List<PointVenteDTO> getAllPointVente();
    PointVenteDTO getPointVente(PointVente pointVente);

}
