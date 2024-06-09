package lab.hang.boulangerie.entity;

import jakarta.persistence.*;
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
@Entity
@Table(name = "produits")
public class Products {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productID;
    private String productName;
    private boolean isActive;
    private int prix;

    @OneToOne
    @JoinColumn(name = "production_standard")
    @MapsId
    private StandardProduction standardProduction;
    @CreationTimestamp
    @DateTimeFormat(pattern = "dd-MM-YYYY")
    private LocalDateTime dateCreated;

}
