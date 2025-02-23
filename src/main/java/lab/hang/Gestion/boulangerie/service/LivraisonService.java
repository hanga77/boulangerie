package lab.hang.Gestion.boulangerie.service;

import jakarta.transaction.Transactional;
import lab.hang.Gestion.boulangerie.dto.CreateLivraisonRequest;
import lab.hang.Gestion.boulangerie.dto.LivraisonDTO;
import lab.hang.Gestion.boulangerie.dto.ProduitLivreRequest;
import lab.hang.Gestion.boulangerie.exception.ResourceNotFoundException;
import lab.hang.Gestion.boulangerie.mapper.LivraisonMapper;
import lab.hang.Gestion.boulangerie.model.*;
import lab.hang.Gestion.boulangerie.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class LivraisonService {
    private final LivraisonRepository livraisonRepository;
    private final ProductionRepository productionRepository;
    private final ProduitRepository produitRepository;
    private final UserService userService;
    private final ProductionService productionService;
    private final LivraisonMapper livraisonMapper;
    private  final TransactionRepository transactionRepository;
    private final CompteBancaireRepository compteBancaireRepository;


    // Constructor omitted for brevity...


    public LivraisonService(LivraisonRepository livraisonRepository,
                            ProductionRepository productionRepository,
                            ProduitRepository produitRepository,
                            UserService userService,
                            ProductionService productionService,
                            LivraisonMapper livraisonMapper, TransactionRepository transactionRepository, CompteBancaireRepository compteBancaireRepository) {
        this.livraisonRepository = livraisonRepository;
        this.productionRepository = productionRepository;
        this.produitRepository = produitRepository;
        this.userService = userService;
        this.productionService = productionService;
        this.livraisonMapper = livraisonMapper;
        this.transactionRepository = transactionRepository;
        this.compteBancaireRepository = compteBancaireRepository;
    }

    public LivraisonDTO createLivraison(CreateLivraisonRequest request) {
        User currentUser = userService.getCurrentUser();
        Production production = getAndValidateProduction(request.getProductionId());

        Livraison livraison = new Livraison();
        livraison.setDateLivraison(LocalDate.now());
        livraison.setNomClient(request.getNomClient());
        livraison.setProduction(production);
        livraison.setUser(currentUser);

        Map<Produit, ProduitLivre> produitsLivres = new HashMap<>();
        Map<Produit, Integer> produitsRestants = production.getProduitsRestants();
        double montantTotal = processProduitsLivraison(request, produitsLivres, produitsRestants);

        livraison.setProduitsLivres(produitsLivres);
        livraison.setMontantTotal(montantTotal);
        production.setProduitsRestants(produitsRestants);

        Livraison savedLivraison = livraisonRepository.save(livraison);
        productionRepository.save(production);

        return livraisonMapper.toDTO(savedLivraison);
    }

    public List<LivraisonDTO> getLivraisonsByDateRange(LocalDate startDate, LocalDate endDate) {
        return livraisonRepository.findByDateLivraisonBetween(startDate, endDate)
                .stream()
                .map(livraisonMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<LivraisonDTO> getLivraisonsByProduction(Long productionId) {
        return livraisonRepository.findByProductionId(productionId)
                .stream()
                .map(livraisonMapper::toDTO)
                .collect(Collectors.toList());
    }

    public LivraisonDTO getLivraisonById(Long id) {
        return livraisonRepository.findById(id)
                .map(livraisonMapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Livraison non trouvée"));
    }

    private Production getAndValidateProduction(Long productionId) {
        Production production = productionRepository.findById(productionId)
                .orElseThrow(() -> new ResourceNotFoundException("Production non trouvée"));

        LocalDate today = LocalDate.now();
        if (!production.getDateProduction().equals(today) &&
                !production.getDateProduction().equals(today.minusDays(1))) {
            throw new IllegalArgumentException("La production doit être du jour même ou de la veille");
        }

        return production;
    }

    private double processProduitsLivraison(CreateLivraisonRequest request,
                                            Map<Produit, ProduitLivre> produitsLivres,
                                            Map<Produit, Integer> produitsRestants) {
        double montantTotal = 0;

        for (Map.Entry<Long, ProduitLivreRequest> entry : request.getProduits().entrySet()) {
            Produit produit = produitRepository.findById(entry.getKey())
                    .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé"));

            ProduitLivreRequest produitRequest = entry.getValue();
            validateAndUpdateStock(produit, produitRequest.getQuantite(), produitsRestants);

            ProduitLivre produitLivre = createProduitLivre(produit, produitRequest);
            produitsLivres.put(produit, produitLivre);
            montantTotal += produitRequest.getPrixVente() * produitRequest.getQuantite();
        }

        return montantTotal;
    }

    private void validateAndUpdateStock(Produit produit, int quantiteDemandee, Map<Produit, Integer> produitsRestants) {
        int stockDisponible = produitsRestants.getOrDefault(produit, 0);
        if (stockDisponible < quantiteDemandee) {
            throw new IllegalArgumentException(
                    String.format("Stock insuffisant pour %s. Disponible: %d, Demandé: %d",
                            produit.getNom(), stockDisponible, quantiteDemandee)
            );
        }
        produitsRestants.put(produit, stockDisponible - quantiteDemandee);
    }

    private ProduitLivre createProduitLivre(Produit produit, ProduitLivreRequest request) {
        ProduitLivre produitLivre = new ProduitLivre();
        produitLivre.setQuantite(request.getQuantite());
        produitLivre.setPrixInitial(produit.getPrix());
        produitLivre.setPrixVente(request.getPrixVente());
        return produitLivre;
    }

    public List<LivraisonDTO> getAllLivraisons() {
        return livraisonRepository.findAll().stream().map(livraisonMapper::toDTO).collect(Collectors.toList());
    }
    public Page<LivraisonDTO> getAllLivraisons(Pageable pageable) {
        return livraisonRepository.findAll(pageable).map(livraisonMapper::toDTO);
    }

    @Transactional
    public void enregistrerRevenuLivraison(Long livraisonId, double montantTotal) {
        Livraison livraison = livraisonRepository.findById(livraisonId)
                .orElseThrow(() -> new RuntimeException("Livraison non trouvée avec l'ID : " + livraisonId));

        CompteBancaire compte = compteBancaireRepository.findByNom("Compte Principal");
        compte.setSolde(compte.getSolde() + montantTotal);

        Transaction transaction = new Transaction();
        transaction.setDate(LocalDate.now());
        transaction.setType("VENTE");
        transaction.setMontant(montantTotal);
        transaction.setDescription("Revenu de la livraison ID: " + livraisonId);
        transaction.setCompteBancaire(compte);

        transactionRepository.save(transaction);
        compteBancaireRepository.save(compte);
    }

    @Transactional
    public double calculerMontantLivraison(Long livraisonId) {
        Livraison livraison = livraisonRepository.findById(livraisonId)
                .orElseThrow(() -> new RuntimeException("Livraison non trouvée avec l'ID : " + livraisonId));

        /*double montantTotal = 0;

        // Parcourir les produits livrés
        for (Map.Entry<Produit, ProduitLivre> entry : livraison.getProduitsLivres().entrySet()) {
            Produit produit = entry.getKey();
            ProduitLivre produitLivre = entry.getValue();
            double prixVente = produitLivre.getPrixVente();
            int quantiteLivree = produitLivre.getQuantite();
            montantTotal += prixVente * quantiteLivree;
        }*/

        return livraison.getMontantTotal();
    }

    @Transactional
    public void enregistrerRevenuLivraison(Long livraisonId) {
        double montantTotal = calculerMontantLivraison(livraisonId);

        // Enregistrer la transaction financière
        CompteBancaire compte = compteBancaireRepository.findByNom("Compte Principal");
        compte.setSolde(compte.getSolde() + montantTotal);

        Transaction transaction = new Transaction();
        transaction.setDate(LocalDate.now());
        transaction.setType("VENTE");
        transaction.setMontant(montantTotal);
        transaction.setDescription("Revenu de la livraison ID: " + livraisonId);
        transaction.setCompteBancaire(compte);

        transactionRepository.save(transaction);
        compteBancaireRepository.save(compte);
    }


}
