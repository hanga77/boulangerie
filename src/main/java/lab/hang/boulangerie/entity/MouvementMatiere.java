package lab.hang.boulangerie.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.format.annotation.DateTimeFormat;

import javax.print.attribute.standard.DateTimeAtCreation;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "mouvement_matiere")
public class MouvementMatiere {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ID;
    @ManyToOne(cascade=CascadeType.ALL)
    @JoinColumn(name = "matiere_premiere")
    @MapsId
    private Matiere matiereID;

    @ManyToOne(cascade=CascadeType.ALL)
    @JoinColumn(name = "users_id")
    @MapsId
    private Users usersId;

    private String actions;
    private int qte_stock;

    @CreationTimestamp
    @DateTimeFormat(pattern = "dd-MM-YYYY")
    private LocalDateTime dateCreated;

    @ManyToOne(cascade=CascadeType.ALL)
    @JoinColumn(name = "session_id")
    private  Session sessionID;
}
