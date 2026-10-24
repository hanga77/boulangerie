package lab.hang.Gestion.boulangerie.infrastructure.persistence;

import lab.hang.Gestion.boulangerie.application.sales.CatalogSalesPort;
import lab.hang.Gestion.boulangerie.application.sales.SalesPersistencePort;
import lab.hang.Gestion.boulangerie.domain.sales.LivraisonV2;
import lab.hang.Gestion.boulangerie.domain.sales.VenteLibreV2;
import lab.hang.Gestion.boulangerie.model.CompteBancaire;
import lab.hang.Gestion.boulangerie.model.Produit;
import lab.hang.Gestion.boulangerie.repository.CompteBancaireRepository;
import lab.hang.Gestion.boulangerie.repository.ProduitRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
public class JpaSalesAdapter implements SalesPersistencePort, CatalogSalesPort {

    private final LivraisonV2Repository livraisonRepo;
    private final VenteLibreV2Repository venteRepo;
    private final ProduitRepository produitRepo;
    private final CompteBancaireRepository compteRepo;

    public JpaSalesAdapter(LivraisonV2Repository livraisonRepo,
                           VenteLibreV2Repository venteRepo,
                           ProduitRepository produitRepo,
                           CompteBancaireRepository compteRepo) {
        this.livraisonRepo = livraisonRepo;
        this.venteRepo = venteRepo;
        this.produitRepo = produitRepo;
        this.compteRepo = compteRepo;
    }

    @Override
    public LivraisonV2 saveLivraison(LivraisonV2 livraison) {
        return livraisonRepo.save(livraison);
    }

    @Override
    public VenteLibreV2 saveVente(VenteLibreV2 vente) {
        return venteRepo.save(vente);
    }

    @Override
    public List<LivraisonV2> findLivraisonsByProduction(Long productionV2Id) {
        return livraisonRepo.findByProductionV2Id(productionV2Id);
    }

    @Override
    public List<VenteLibreV2> findVentesByProduction(Long productionV2Id) {
        return venteRepo.findByProductionV2Id(productionV2Id);
    }

    @Override
    public int countLivraisonsByProduction(Long productionV2Id) {
        return livraisonRepo.countByProductionV2Id(productionV2Id);
    }

    @Override
    public int countVentesByDate(LocalDate date) {
        return venteRepo.countByDateVente(date);
    }

    @Override
    @Transactional
    public void crediterCompte(Long compteId, double montant) {
        CompteBancaire c = compteRepo.findById(compteId)
                .orElseThrow(() -> new IllegalArgumentException("Compte introuvable"));
        c.setSolde(c.getSolde() + montant);
        compteRepo.save(c);
    }

    @Override
    public double prixProduit(Long produitId) {
        return produitRepo.findById(produitId).map(Produit::getPrix).orElse(0.0);
    }

    @Override
    public String nomProduit(Long produitId) {
        return produitRepo.findById(produitId).map(Produit::getNom).orElse("?");
    }
}
