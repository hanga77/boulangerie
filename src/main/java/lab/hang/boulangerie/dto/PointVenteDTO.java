package lab.hang.boulangerie.dto;


import lab.hang.boulangerie.entity.Commande;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PointVenteDTO {
    private Long pointVenteID;
    private String smallName;
    private List<Commande> commandeList;
    @CreationTimestamp
    @DateTimeFormat(pattern = "dd-MM-YYYY")
    private LocalDateTime dateCreated;
}
