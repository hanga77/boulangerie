package lab.hang.boulangerie.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "standard_production")
public class StandardProduction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  Long IDStandard;

    @ManyToOne(cascade=CascadeType.ALL)
    @JoinColumn(name = "product_id")
    @MapsId
    private  Products products;

    @OneToMany(targetEntity = LigneStandard.class, mappedBy = "standardProduction",cascade = CascadeType.ALL)
    private List<LigneStandard> ligneStandards;


}
