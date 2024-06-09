package lab.hang.boulangerie.repository;

import lab.hang.boulangerie.entity.Products;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Products,Long> {

    Optional<Products> findProductsByProductName(String products);
}
