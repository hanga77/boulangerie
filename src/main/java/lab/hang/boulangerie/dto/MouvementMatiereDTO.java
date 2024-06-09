package lab.hang.boulangerie.dto;


import jakarta.persistence.CascadeType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import lab.hang.boulangerie.entity.Matiere;
import lab.hang.boulangerie.entity.Session;
import lab.hang.boulangerie.entity.Users;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;


import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MouvementMatiereDTO {
    private Long ID;
    private Matiere matiereID;
    private Users usersId;
    private String actions;
    private int qte_stock;
    @CreationTimestamp
    private LocalDateTime dateCreated;
    private  Session sessionID;
}
