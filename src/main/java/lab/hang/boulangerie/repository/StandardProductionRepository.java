package lab.hang.boulangerie.repository;

import lab.hang.boulangerie.dto.ProductDTO;
import lab.hang.boulangerie.entity.StandardProduction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StandardProductionRepository extends JpaRepository<StandardProduction, Long> {
    StandardProduction findByProducts(ProductDTO productDTO);
}
