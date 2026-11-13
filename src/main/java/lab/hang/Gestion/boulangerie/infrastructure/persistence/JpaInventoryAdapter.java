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

        double stockAvant = m.getStock();
        double prixAvant = m.getPrixUnitaire();
        double stockApres = stockAvant + quantite;

        if (stockApres > 0) {
            double cmp = (stockAvant * prixAvant + quantite * prixUnitaireEntree) / stockApres;
            m.setPrixUnitaire(Math.round(cmp * 100.0) / 100.0);
        } else {
            m.setPrixUnitaire(prixUnitaireEntree);
        }
        m.setStock(stockApres);
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

        StockMovement mv = new StockMovement();
        mv.setType("SORTIE");
        mv.setQuantite(quantite);
        mv.setDate(LocalDate.now());
        mv.setMatierePremiere(m);
        mv.setUser(user);
        mv.setMotif("SORTIE_PROD_V2");
        mv.setProductionV2Id(productionId); // phase 4 — traçabilité V2
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
        return 90;
    }
}
