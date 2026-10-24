package lab.hang.Gestion.boulangerie.infrastructure.persistence;

import lab.hang.Gestion.boulangerie.application.inventory.InventoryPort;
import lab.hang.Gestion.boulangerie.domain.inventory.LotV2;
import lab.hang.Gestion.boulangerie.model.MatierePremiere;
import lab.hang.Gestion.boulangerie.model.StockMovement;
import lab.hang.Gestion.boulangerie.model.User;
import lab.hang.Gestion.boulangerie.repository.MatierePremiereRepository;
import lab.hang.Gestion.boulangerie.repository.StockMovementRepository;
import lab.hang.Gestion.boulangerie.repository.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
public class JpaInventoryAdapter implements InventoryPort {

    private final MatierePremiereRepository matiereRepo;
    private final StockMovementRepository movementRepo;
    private final LotV2Repository lotRepo;
    private final UserRepository userRepo;

    public JpaInventoryAdapter(MatierePremiereRepository matiereRepo,
                               StockMovementRepository movementRepo,
                               LotV2Repository lotRepo,
                               UserRepository userRepo) {
        this.matiereRepo = matiereRepo;
        this.movementRepo = movementRepo;
        this.lotRepo = lotRepo;
        this.userRepo = userRepo;
    }

    @Override
    public void saveLot(LotV2 lot) {
        lotRepo.save(lot);
    }

    @Override
    @Transactional
    public void augmenterStock(Long matiereId, double quantite, double prixUnitaireEntree) {
        MatierePremiere m = matiereRepo.findById(matiereId)
                .orElseThrow(() -> new IllegalArgumentException("Matière introuvable: " + matiereId));
        m.setStock(m.getStock() + quantite);
        m.setPrixUnitaire(prixUnitaireEntree);
        matiereRepo.save(m);
    }

    @Override
    @Transactional
    public void diminuerStockAvecVerrou(Long matiereId, double quantite) {
        MatierePremiere m = matiereRepo.findByIdWithLock(matiereId)
                .orElseThrow(() -> new IllegalArgumentException("Matière introuvable: " + matiereId));
        if (m.getStock() < quantite) {
            throw new IllegalStateException("Stock insuffisant: " + m.getNom());
        }
        m.setStock(m.getStock() - quantite);
        matiereRepo.save(m);
    }

    @Override
    @Transactional
    public void enregistrerMouvementEntree(Long matiereId, double quantiteNette,
                                           double quantiteCommandee, double quantiteAvariee,
                                           Long userId) {
        MatierePremiere m = matiereRepo.findById(matiereId).orElseThrow();
        User user = userRepo.findById(userId).orElse(null);

        StockMovement mv = new StockMovement();
        mv.setType("ENTREE");
        mv.setQuantite(quantiteNette);
        mv.setDate(LocalDate.now());
        mv.setMatierePremiere(m);
        mv.setUser(user);
        mv.setMotif("ENTREE");
        mv.setQuantiteCommandee(quantiteCommandee);
        mv.setQuantiteAvariee(quantiteAvariee > 0 ? quantiteAvariee : null);
        movementRepo.save(mv);
    }

    @Override
    @Transactional
    public void enregistrerMouvementSortieProduction(Long matiereId, double quantite,
                                                     Long productionId, Long userId) {
        MatierePremiere m = matiereRepo.findById(matiereId).orElseThrow();
        User user = userRepo.findById(userId).orElse(null);

        // production_id FK pointe encore vers l'ancienne table Production ;
        // on trace la V2 dans le motif jusqu'à migration complète du FK.
        StockMovement mv = new StockMovement();
        mv.setType("SORTIE");
        mv.setQuantite(quantite);
        mv.setDate(LocalDate.now());
        mv.setMatierePremiere(m);
        mv.setUser(user);
        mv.setMotif("SORTIE_PROD_V2:" + productionId);
        movementRepo.save(mv);
    }

    @Override
    public String nomMatiere(Long matiereId) {
        return matiereRepo.findById(matiereId).map(MatierePremiere::getNom).orElse("?");
    }

    @Override
    public String uniteMesure(Long matiereId) {
        return matiereRepo.findById(matiereId).map(MatierePremiere::getUniteMesure).orElse("");
    }

    @Override
    public double stockActuel(Long matiereId) {
        return matiereRepo.findById(matiereId).map(MatierePremiere::getStock).orElse(0.0);
    }

    @Override
    public int dureeConservationDefaut(Long matiereId) {
        // Pas encore de colonne dédiée : 90 jours par défaut
        return 90;
    }
}
