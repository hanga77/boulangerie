package lab.hang.boulangerie.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "commande_produit")
public class Commande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commandeID;
    @ManyToOne(cascade=CascadeType.ALL)
    @JoinColumn(name = "vente_Id")
    private PointVente pointVenteID;
    @OneToMany(targetEntity = LigneCommande.class,mappedBy = "commande",cascade = CascadeType.ALL)
    private List<LigneCommande> ligneCommandes;

    @ManyToOne(cascade=CascadeType.ALL)
    @JoinColumn(name = "session_Id")
    private Session session;
    private Double montantTotal;
    @CreationTimestamp
    @DateTimeFormat(pattern = "dd-MM-YYYY")
    private LocalDateTime dateCreated;

}
