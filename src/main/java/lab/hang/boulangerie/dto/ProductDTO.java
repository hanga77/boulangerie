package lab.hang.boulangerie.dto;

import lab.hang.boulangerie.entity.StandardProduction;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ProductDTO {
    private Long productID;
    private String productName;
    @DateTimeFormat(pattern = "dd-MM-YYYY")
    private LocalDateTime dateCreated;
    private StandardProduction standardProduction;
    private boolean isActive;
    private int prix;
}
