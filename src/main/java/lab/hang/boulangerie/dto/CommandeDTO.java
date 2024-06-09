package lab.hang.boulangerie.dto;


import lab.hang.boulangerie.entity.LigneCommande;
import lab.hang.boulangerie.entity.PointVente;
import lab.hang.boulangerie.entity.Session;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CommandeDTO {

    private Long commandeID;

    private PointVente pointVenteID;
    private List<LigneCommande> ligneCommandes;

    private Session session;
    private Double montantTotal;

    @DateTimeFormat(pattern = "dd-MM-YYYY")
    private LocalDateTime dateCreated;
}
