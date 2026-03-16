package lab.hang.Gestion.boulangerie.service;

import jakarta.transaction.Transactional;
import lab.hang.Gestion.boulangerie.exception.MatierePremiereNotFoundException;
import lab.hang.Gestion.boulangerie.exception.ProductionNotFoundException;
import lab.hang.Gestion.boulangerie.exception.StockInsuffisantException;
import lab.hang.Gestion.boulangerie.dto.CommandeDTO;
import lab.hang.Gestion.boulangerie.dto.ProductionDTO;
import lab.hang.Gestion.boulangerie.dto.ProduitDTO;
import lab.hang.Gestion.boulangerie.mapper.CommandeMapper;
import lab.hang.Gestion.boulangerie.mapper.ProductionMapper;
import lab.hang.Gestion.boulangerie.model.*;
import lab.hang.Gestion.boulangerie.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductionService {


    private final ProductionRepository productionRepository;
    private final MatierePremiereRepository matierePremiereRepository;

    private final CommandeRepository commandeRepository;
    private final CommandeService commandeService;
    private final ProductionMapper productionMapper;

    private final MatierePremiereService matierePremiereService;

    private final ProduitService produitService;

    private final CommandeMapper commandeMapper;
    private  final CompteBancaireRepository compteBancaireRepository;
    private  final TransactionRepository transactionRepository;

    public ProductionService(ProductionRepository productionRepository, MatierePremiereRepository matierePremiereRepository, CommandeRepository commandeRepository, CommandeService commandeService, ProductionMapper productionMapper, MatierePremiereService matierePremiereService, ProduitService produitService, CommandeMapper commandeMapper,
                             CompteBancaireRepository compteBancaireRepository, TransactionRepository transactionRepository) {
        this.productionRepository = productionRepository;
        this.matierePremiereRepository = matierePremiereRepository;
        this.commandeRepository = commandeRepository;
        this.commandeService = commandeService;
        this.productionMapper = productionMapper;
        this.matierePremiereService = matierePremiereService;
        this.produitService = produitService;
        this.commandeMapper = commandeMapper;
        this.compteBancaireRepository = compteBancaireRepository;
        this.transactionRepository = transactionRepository;
    }

    public ProductionDTO startProduction(LocalDate dateProduction, User user) {
        List<CommandeDTO> commandesDuJour = commandeService.getCommandesByDateAndEtat(dateProduction);
        Map<Long, Integer> produitsProduits = new HashMap<>();
        Map<Long, Double> matieresUtilisees = new HashMap<>();

        /*System.out.println("Starting production for date: " + dateProduction);
        System.out.println("Number of orders to process: " + commandesDuJour.size());
*/
        for (CommandeDTO commande : commandesDuJour) {
           /* System.out.println("Processing order ID: " + commande.getId());*/

            for (Map.Entry<Long, Integer> entry : commande.getProduitsCommandes().entrySet()) {
                Long produitId = entry.getKey();
                int quantiteCommandee = entry.getValue();

                // Aggregate products
                produitsProduits.merge(produitId, quantiteCommandee, Integer::sum);

                // Get product details
                ProduitDTO produitDTO = produitService.getProduitById(produitId);
                /*System.out.println("Processing product: " + produitDTO.getNom() +
                        " (ID: " + produitId + "), quantity: " + quantiteCommandee);*/

                // Calculate required materials
                Map<MatierePremiere, Double> matieresPourProduit =
                        produitService.calculateMatieresPremieresNecessaires(produitDTO, quantiteCommandee);

                // Aggregate materials
                matieresPourProduit.forEach((matiere, quantite) ->
                        matieresUtilisees.merge(matiere.getId(), quantite, Double::sum));
            }
        }

        // Ajouter le quota de vente libre pour chaque produit défini
        for (ProduitDTO produit : produitService.getAllProduits()) {
            if (produit.getQuantiteVenteLibreJournaliere() > 0) {
                produitsProduits.merge(produit.getId(), produit.getQuantiteVenteLibreJournaliere(), Integer::sum);
                Map<MatierePremiere, Double> matieresPourQuota =
                        produitService.calculateMatieresPremieresNecessaires(produit, produit.getQuantiteVenteLibreJournaliere());
                matieresPourQuota.forEach((matiere, quantite) ->
                        matieresUtilisees.merge(matiere.getId(), quantite, Double::sum));
            }
        }

        // Create and populate ProductionDTO
        ProductionDTO productionDTO = new ProductionDTO();
        productionDTO.setDateProduction(dateProduction);
        productionDTO.setMatieresPremieresUtilisees(matieresUtilisees);
        productionDTO.setProduitsProduits(produitsProduits);
        productionDTO.setUserId(user.getId());
        productionDTO.setQuantitesReellesUtilisees(new HashMap<>());
        productionDTO.setProduitsRestants(new HashMap<>(produitsProduits));

        // Save production
        Production savedProduction = productionRepository.save(productionMapper.toEntity(productionDTO));

        // Update orders
        for (CommandeDTO commande : commandesDuJour) {
            Commande commandeEntity = commandeMapper.toEntity(commande);
            commandeEntity.setProduction(savedProduction);
            commandeEntity.setUser(user);
            commandeEntity.setProcessed(true);
            commandeRepository.save(commandeEntity);
        }

        return productionMapper.toDTO(savedProduction);
    }

    public void updateProduitsRestants(ProductionDTO productionDTO, Map<Long, Integer> quantitesLivrees) {
        Map<Long, Integer> produitsRestants = productionDTO.getProduitsRestants();

        for (Map.Entry<Long, Integer> entry : quantitesLivrees.entrySet()) {
            Long produitId = entry.getKey();
            Integer quantiteLivree = entry.getValue();

            // Vérifier si la quantité livrée est valide
            Integer quantiteRestante = produitsRestants.getOrDefault(produitId, 0);
            if (quantiteLivree > quantiteRestante) {
                throw new IllegalArgumentException("Quantité livrée supérieure à la quantité restante pour le produit ID : " + produitId);
            }

            // Mettre à jour les quantités restantes
            produitsRestants.put(produitId, quantiteRestante - quantiteLivree);
        }

        // Enregistrer les quantités restantes mises à jour
        productionDTO.setProduitsRestants(produitsRestants);
        productionRepository.save(productionMapper.toEntity(productionDTO));
    }

    @Transactional
    public void updateProduction(ProductionDTO productionDTO) {
        // 1. Récupérer la production existante
        Production production = productionRepository.findById(productionDTO.getId())
                .orElseThrow(() -> new ProductionNotFoundException("Production non trouvée avec l'ID : " + productionDTO.getId()));

        // 2. Mettre à jour les quantités réelles
        Map<MatierePremiere, Double> quantitesReelles = new HashMap<>();
        for (Map.Entry<Long, Double> entry : productionDTO.getQuantitesReellesUtilisees().entrySet()) {
            // Parse the key as Long if it's a String
            Long matiereId = entry.getKey();
            MatierePremiere matiere = matierePremiereService.getMatierePremiereById(matiereId);
            quantitesReelles.put(matiere, entry.getValue());
        }
        production.setQuantitesReellesUtilisees(quantitesReelles);

        // 3. Mettre à jour les produits produits
        Map<Produit, Integer> produitsProduits = new HashMap<>();
        for (Map.Entry<Long, Integer> entry : productionDTO.getProduitsProduits().entrySet()) {
            // Parse the key as Long if it's a String
            Long produitId = entry.getKey();

            Produit produit = produitService.getProduitEntityById(produitId);
            produitsProduits.put(produit, entry.getValue());
        }
        production.setProduitsProduits(produitsProduits);
        production.setProduitsRestants(produitsProduits);

        // 4. Sauvegarder les modifications
        productionRepository.save(production);
    }

    private void updateMatierePremiereStocks(Map<String, Double> quantitesUtilisees) {
        for (Map.Entry<String, Double> entry : quantitesUtilisees.entrySet()) {
            MatierePremiere matierePremiere = matierePremiereService.getMatierePremiereById(Long.valueOf(entry.getKey()));
            Double quantiteUtilisee = entry.getValue();

            // Soustraire la quantité utilisée du stock
            double nouveauStock = matierePremiere.getStock() - quantiteUtilisee;
            if (nouveauStock < 0) {
                throw new StockInsuffisantException("Stock insuffisant pour la matière première : " + matierePremiere.getNom());
            }

            matierePremiere.setStock(nouveauStock);
            matierePremiereRepository.save(matierePremiere);
        }
    }


    public ProductionDTO getProductionById(Long productionId) {
        // 1. Récupérer la production
        Production production = productionRepository.findById(productionId)
                .orElseThrow(() -> new ProductionNotFoundException("Production non trouvée avec l'ID : " + productionId));

        return productionMapper.toDTO(production);
    }


    public Page<Production> getProductionsByDateRange(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return productionRepository.findByDateProductionBetween(startDate, endDate, pageable);
    }

    public List<Production> getProductionsByDateRange(LocalDate startDate, LocalDate endDate) {
        return productionRepository.findByDateProductionBetween(startDate, endDate);
    }

    public Page<Production> getAllProductions(Pageable pageable) {
        return productionRepository.findAll(pageable);
    }
    public List<Production> getAllProductions() {
        return productionRepository.findAll();
    }

    public boolean verifierStocksSuffisants(ProductionDTO productionDTO) {
        Map<Long, Double> matieresPremieresUtilisees = productionDTO.getMatieresPremieresUtilisees();

        for (Map.Entry<Long, Double> entry : matieresPremieresUtilisees.entrySet()) {
            Long matiereId = entry.getKey();
            Double quantiteNecessaire = entry.getValue();

            MatierePremiere matiere = matierePremiereRepository.findById(matiereId)
                    .orElseThrow(() -> new MatierePremiereNotFoundException("Matière première non trouvée avec l'ID : " + matiereId));

            if (matiere.getStock() < quantiteNecessaire) {
                return false; // Stock insuffisant
            }
        }

        return true; // Stocks suffisants
    }

    @Transactional
    public double calculerCoutProduction(Long productionId) {
        Production production = productionRepository.findById(productionId)
                .orElseThrow(() -> new ProductionNotFoundException("Production non trouvée avec l'ID : " + productionId));

        double coutTotal = 0;

        // Parcourir les matières premières utilisées dans la production
        for (Map.Entry<MatierePremiere, Double> entry : production.getMatieresPremieresUtilisees().entrySet()) {
            MatierePremiere matiere = entry.getKey();
            double quantiteUtilisee = entry.getValue();
            double prixUnitaire = matiere.getPrixUnitaire(); // Supposons que chaque matière première a un prix unitaire
            coutTotal += quantiteUtilisee * prixUnitaire;
        }

        return coutTotal;
    }

    @Transactional
    public void enregistrerCoutProduction(Long productionId) {
        double coutTotal = calculerCoutProduction(productionId);

        // Enregistrer la transaction financière
        CompteBancaire compte = compteBancaireRepository.findByNom("Compte Principal");
        compte.setSolde(compte.getSolde() - coutTotal);

        Transaction transaction = new Transaction();
        transaction.setDate(LocalDate.now());
        transaction.setType("PRODUCTION");
        transaction.setMontant(coutTotal);
        transaction.setDescription("Coût de production pour la production ID: " + productionId);
        transaction.setCompteBancaire(compte);

        transactionRepository.save(transaction);
        compteBancaireRepository.save(compte);
    }

    public List<Production> getProductionsByDate(LocalDate now) {
        return productionRepository.findByDateProduction(now);
    }

    public double calculerCoutTotalProduction(LocalDate debutMois, LocalDate finMois) {
        double coutTotal = 0;

        for (Production production : getProductionsByDateRange(debutMois, finMois)) {
            coutTotal += calculerCoutProduction(production.getId());
        }

        return coutTotal;
    }

   public List<?> getVentesParProduit(LocalDate debutMois, LocalDate finMois) {
        return transactionRepository.findByDateBetweenAndType("VENTE", debutMois, finMois);
    }
}