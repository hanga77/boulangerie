package lab.hang.Gestion.boulangerie.infrastructure.persistence;

import lab.hang.Gestion.boulangerie.application.inventory.ProductionPort;
import lab.hang.Gestion.boulangerie.application.production.ProductionPersistencePort;
import lab.hang.Gestion.boulangerie.domain.production.ProductionV2;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
public class JpaProductionAdapter implements ProductionPort, ProductionPersistencePort {

    private final ProductionV2Repository repo;

    public JpaProductionAdapter(ProductionV2Repository repo) {
        this.repo = repo;
    }

    @Override
    @Transactional(readOnly = true)
    public ProductionV2 getById(Long id) {
        ProductionV2 p = repo.findByIdWithLignes(id)
                .or(() -> repo.findById(id))
                .orElseThrow(() -> new IllegalArgumentException("Production V2 introuvable: " + id));
        // Initialise le 2e bag en lazy (même transaction) — pas de double JOIN FETCH
        p.getLignesMatieres().size();
        return p;
    }

    @Override
    public ProductionV2 save(ProductionV2 production) {
        return repo.save(production);
    }

    @Override
    public boolean existsForDate(LocalDate date) {
        return repo.existsByDateProduction(date);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductionV2> findByDate(LocalDate date) {
        List<ProductionV2> list = repo.findByDateProduction(date);
        list.forEach(p -> {
            p.getLignesProduits().size();
            p.getLignesMatieres().size();
        });
        return list;
    }
}
