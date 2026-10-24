package lab.hang.Gestion.boulangerie.application.production;

import lab.hang.Gestion.boulangerie.domain.production.ProductionV2;

import java.time.LocalDate;

public interface ProductionPersistencePort {
    boolean existsForDate(LocalDate date);
    ProductionV2 save(ProductionV2 production);
    ProductionV2 getById(Long id);
}
