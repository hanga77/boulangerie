package lab.hang.Gestion.boulangerie.application.inventory;

import lab.hang.Gestion.boulangerie.domain.production.ProductionV2;

public interface ProductionPort {
    ProductionV2 getById(Long id);
    ProductionV2 save(ProductionV2 production);
}
