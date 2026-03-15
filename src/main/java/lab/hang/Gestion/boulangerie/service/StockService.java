package lab.hang.Gestion.boulangerie.service;

import lab.hang.Gestion.boulangerie.exception.*;
import lab.hang.Gestion.boulangerie.model.*;
import lab.hang.Gestion.boulangerie.repository.CompteBancaireRepository;
import lab.hang.Gestion.boulangerie.repository.LotRepository;
import lab.hang.Gestion.boulangerie.repository.StockMovementRepository;
import lab.hang.Gestion.boulangerie.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StockService {

    private static final Logger log = LoggerFactory.getLogger(StockService.class);

    private final StockMovementRepository stockMovementRepository;
    private final MatierePremiereService matierePremiereService;

    private final LotRepository lotRepository;
    private final UserService userService;

    private final CompteBancaireRepository compteBancaireRepository;
    private final TransactionRepository transactionRepository;




    public StockService(StockMovementRepository stockMovementRepository, MatierePremiereService matierePremiereService, LotRepository lotRepository, UserService userService, CompteBancaireRepository compteBancaireRepository, TransactionRepository transactionRepository) {
        this.stockMovementRepository = stockMovementRepository;
        this.matierePremiereService = matierePremiereService;
        this.lotRepository = lotRepository;
        this.userService = userService;
        this.compteBancaireRepository = compteBancaireRepository;
        this.transactionRepository = transactionRepository;
    }

    /*@Transactional
    public void addStock(Long matierePremiereId, double quantite) {
        MatierePremiere matierePremiere = matierePremiereService.getMatierePremiereById(matierePremiereId);
        matierePremiere.setStock(matierePremiere.getStock() + quantite);
        Lot lot = new Lot();
        lot.setQuantite(quantite);
        lot.setDatePeremption(LocalDate.now().plusDays(32));
        lot.setMatierePremiere(matierePremiere);


        lotRepository.save(lot);
        matierePremiereService.saveMatierePremiere(matierePremiere);
        recordStockMovement(matierePremiere, quantite, "ENTREE");
    }*/






    @Transactional
    public void addStock(Long matierePremiereId, double quantite, double prixUnitaire) {
        // Input validation
        if (matierePremiereId == null) {
            throw new IllegalArgumentException("L'ID de la matière première ne peut pas être null");
        }
        if (quantite <= 0 || prixUnitaire <= 0) {
            throw new IllegalArgumentException("La quantité et le prix unitaire doivent être positifs");
        }

        // Get current user
        User currentUser = userService.getCurrentUser();
        if (currentUser == null || currentUser.getId() == null) {
            throw new SecurityException("Utilisateur non authentifié ou ID utilisateur manquant");
        }

        // Get matiere premiere
        MatierePremiere matierePremiere = matierePremiereService.getMatierePremiereById(matierePremiereId);
        if (matierePremiere == null) {
            throw new EntityNotFoundException("Matière première non trouvée avec l'ID: " + matierePremiereId);
        }

        // Get compte bancaire
        CompteBancaire compte = compteBancaireRepository.findByNom("Compte Principal");
        if (compte == null) {
            throw new EntityNotFoundException("Compte bancaire principal non trouvé");
        }

        // Calculate total cost
        double coutTotal = quantite * prixUnitaire;


        try {
            // Update stock
            matierePremiere.setStock(matierePremiere.getStock() + quantite);
            matierePremiere.setPrixUnitaire(prixUnitaire);

            // Create new lot
            Lot lot = new Lot();
            lot.setQuantite(quantite);
            lot.setDatePeremption(LocalDate.now().plusDays(32));
            lot.setMatierePremiere(matierePremiere);
            lotRepository.save(lot);

            log.debug("Enregistrement mouvement stock ENTREE - matière: {}, quantité: {}", matierePremiere.getNom(), quantite);
            // Record stock movement
            recordStockMovement(matierePremiere, quantite, "ENTREE");
            log.debug("Mouvement stock ENTREE enregistré avec succès");

            // Create transaction with credit handling
            boolean isSoldeInsuffisant = compte.getSolde() < coutTotal;
            String transactionType = isSoldeInsuffisant ? "ACHAT_CREDIT" : "ACHAT";
            String description = String.format("Achat de %.2f unités de %s par %s",
                    quantite,
                    matierePremiere.getNom(),
                    currentUser.getUsername());

            if (isSoldeInsuffisant) {
                description += " (CRÉDIT)";
                log.warn("Achat à crédit - utilisateur: {}, montant: {}, solde disponible: {}",
                        currentUser.getUsername(), coutTotal, compte.getSolde());
            }

            Transaction transaction = new Transaction();
            transaction.setDate(LocalDate.now());
            transaction.setType(transactionType);
            transaction.setMontant(coutTotal);
            transaction.setDescription(description);
            transaction.setCompteBancaire(compte);

            // Update account balance
            compte.setSolde(compte.getSolde() - coutTotal);

            // Save all changes
            transactionRepository.save(transaction);
            compteBancaireRepository.save(compte);

            if (isSoldeInsuffisant) {
                log.warn("Achat effectué avec solde insuffisant - dette: {}, matière: {}",
                        Math.abs(compte.getSolde()), matierePremiere.getNom());
            }

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'ajout du stock: " + e.getMessage(), e);
        }
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void removeStock(Long matierePremiereId, double quantite) {
        // Validation des entrées
        if (quantite <= 0) {
            throw new IllegalArgumentException("La quantité doit être positive.");
        }

        // Récupérer la matière première avec un verrou pessimiste
        MatierePremiere matierePremiere = matierePremiereService.getMatierePremiereById(matierePremiereId);

        // Vérifier si le stock est suffisant
        if (matierePremiere.getStock() < quantite) {
            throw new StockInsuffisantException("Stock insuffisant pour la matière première : " + matierePremiere.getNom());
        }

        // Mettre à jour le stock
        matierePremiere.setStock(matierePremiere.getStock() - quantite);

        log.debug("Enregistrement mouvement stock SORTIE - matière: {}, quantité: {}", matierePremiere.getNom(), quantite);
        // Enregistrer le mouvement de stock
        recordStockMovement(matierePremiere, quantite, "SORTIE");
        log.debug("Mouvement stock SORTIE enregistré avec succès");
        // Sauvegarder les modifications
        matierePremiereService.saveMatierePremiere(matierePremiere);
    }


    private void recordStockMovement(MatierePremiere matierePremiere, double quantite, String type) {
        // Récupérer l'utilisateur courant avec gestion explicite des erreurs
        User currentUser;
        try {
            currentUser = userService.getCurrentUser();
            if (currentUser == null) {
                throw new SecurityException("Impossible de récupérer l'utilisateur courant");
            }
            if (currentUser.getId() == null) {
                throw new SecurityException("L'ID de l'utilisateur est manquant");
            }

            log.debug("Enregistrement mouvement - utilisateur: {} (ID: {})", currentUser.getUsername(), currentUser.getId());

            StockMovement movement = new StockMovement();
            movement.setType(type);
            movement.setQuantite(quantite);
            movement.setDate(LocalDate.now());
            movement.setMatierePremiere(matierePremiere);
            movement.setUser(currentUser);
            movement.setMotif(type);

            stockMovementRepository.save(movement);

        } catch (UserNotAuthenticatedException | UserNotFoundException e) {
            throw new SecurityException("Erreur d'authentification: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'enregistrement du mouvement: " + e.getMessage(), e);
        }
    }


    public List<StockMovement> getStockMovementsByDate(LocalDate date) {
        return stockMovementRepository.findByDateOrderByDateDesc(date);
    }


    public Page<StockMovement> getMouvementsByDateRange(LocalDate dateDebut, LocalDate dateFin, PageRequest of) {
        return stockMovementRepository.findByDateBetween(dateDebut,dateFin,of);
    }
    // Méthode pour calculer les stocks disponibles par matière première
    public Map<MatierePremiere, StockSummary> getStockSummary(LocalDate date) {
        Map<MatierePremiere, StockSummary> stockSummaryMap = new HashMap<>();

        // Récupérer toutes les matières premières
        List<MatierePremiere> matieresPremieres = matierePremiereService.getAllMatierePremieres();

        // Pour chaque matière première, calculer les informations
        for (MatierePremiere matiere : matieresPremieres) {
            StockSummary summary = new StockSummary();

            // Stock disponible hier (à la fin de la journée précédente)
            LocalDate hier = date.minusDays(1);
            List<StockMovement> mouvementsHier = stockMovementRepository.findByDateAndMatierePremiere(hier, matiere);
            double stockHier = mouvementsHier.stream()
                    .mapToDouble(m -> m.getType().equals("ENTREE") ? m.getQuantite() : -m.getQuantite())
                    .sum();
            summary.setStockHier(stockHier);

            // Sorties de la journée en cours
            List<StockMovement> sortiesAujourdhui = stockMovementRepository.findByDateAndMatierePremiereAndType(date, matiere, "SORTIE");
            double sortiesJournee = sortiesAujourdhui.stream()
                    .mapToDouble(StockMovement::getQuantite)
                    .sum();
            summary.setSortiesJournee(sortiesJournee);

            // Stock réel disponible en magasin
            double stockReel = matiere.getStock() - sortiesJournee;
            summary.setStockReel(stockReel);

            stockSummaryMap.put(matiere, summary);
        }

        return stockSummaryMap;
    }

    public static class StockSummary {
        private double stockHier; // Stock disponible hier
        private double sortiesJournee; // Sorties de la journée en cours
        private double stockReel; // Stock réel disponible en magasin

        // Getters et Setters
        public double getStockHier() {
            return stockHier;
        }

        public void setStockHier(double stockHier) {
            this.stockHier = stockHier;
        }

        public double getSortiesJournee() {
            return sortiesJournee;
        }

        public void setSortiesJournee(double sortiesJournee) {
            this.sortiesJournee = sortiesJournee;
        }

        public double getStockReel() {
            return stockReel;
        }

        public void setStockReel(double stockReel) {
            this.stockReel = stockReel;
        }
    }
}