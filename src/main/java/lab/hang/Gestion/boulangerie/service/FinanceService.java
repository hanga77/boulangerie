package lab.hang.Gestion.boulangerie.service;

import jakarta.transaction.Transactional;
import lab.hang.Gestion.boulangerie.dto.TransactionDTO;
import lab.hang.Gestion.boulangerie.exception.ResourceNotFoundException;
import lab.hang.Gestion.boulangerie.mapper.TransactionMapper;
import lab.hang.Gestion.boulangerie.model.CompteBancaire;
import lab.hang.Gestion.boulangerie.model.Transaction;
import lab.hang.Gestion.boulangerie.repository.CompteBancaireRepository;
import lab.hang.Gestion.boulangerie.repository.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FinanceService {

    private final TransactionRepository transactionRepository;
    private final CompteBancaireRepository compteBancaireRepository;

    private final AlerteService alerteService;

    public FinanceService(TransactionRepository transactionRepository, CompteBancaireRepository compteBancaireRepository, AlerteService alerteService) {
        this.transactionRepository = transactionRepository;
        this.compteBancaireRepository = compteBancaireRepository;
        this.alerteService = alerteService;
    }

    public double calculerProfit(LocalDate startDate, LocalDate endDate) {
        List<Transaction> transactions = transactionRepository.findByDateBetween(startDate, endDate);
        double revenus = 0;
        double depenses = 0;

        for (Transaction transaction : transactions) {
            if (transaction.getType().equals("VENTE")) {
                revenus += transaction.getMontant(); // Revenus des ventes
            } else if (transaction.getType().equals("PRODUCTION") || transaction.getType().equals("ACHAT")) {
                depenses += transaction.getMontant(); // Dépenses de production et achats
            }
        }

        return revenus - depenses;
    }


    public CompteBancaire getCompteBancaire(Long id) {
        return compteBancaireRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Compte bancaire non trouvé avec l'ID : " + id));
    }

    public List<Transaction> getTransactionsByDateRange(LocalDate startDate, LocalDate endDate) {
        return transactionRepository.findByDateBetween(startDate, endDate);
    }

    public double calculerCoutsProduction() {
        Double total = transactionRepository.sumMontantByType("PRODUCTION");
        return total != null ? total : 0.0;
    }

    public double calculerRevenusVentes() {
        Double total = transactionRepository.sumMontantByType("VENTE");
        return total != null ? total : 0.0;
    }

    public double calculerProfitTotal() {
        double revenusVentes = calculerRevenusVentes();
        double coutsProduction = calculerCoutsProduction();
        double depensesAchats = calculerDepensesAchats();
        return revenusVentes - coutsProduction - depensesAchats;
    }

    public double calculerDepensesAchats() {
        Double total = transactionRepository.sumMontantByType("ACHAT");
        return total != null ? total : 0.0;
    }

    public List<TransactionDTO> getAllTransactions() {
        List<Transaction> transactions = transactionRepository.findAll();
        return transactions.stream()
                .map(TransactionMapper::toDTO)
                .collect(Collectors.toList());
    }
    public Page<TransactionDTO> getAllTransactions(Pageable pageable) {
        Page<Transaction> transactions = transactionRepository.findAll(pageable);
        return transactions.map(TransactionMapper::toDTO);
    }

    public double calculerRevenusTotaux() {
        List<Transaction> transactions = transactionRepository.findByType("VENTE");
        return transactions.stream()
                .mapToDouble(Transaction::getMontant)
                .sum();
    }

    public double calculerDepensesTotales() {
        Double total = transactionRepository.sumMontantByTypeIn(List.of("PRODUCTION", "ACHAT"));
        return total != null ? total : 0.0;
    }

    public double calculerProfit() {
        return calculerProfitTotal();
    }

    public CompteBancaire getCompteBancairePrincipal() {
        CompteBancaire comptePrincipal = compteBancaireRepository.findByNom("Compte Principal");
        if (comptePrincipal == null) {
            throw new ResourceNotFoundException("Compte Principal non trouvé");
        }
        return comptePrincipal;
    }

    public void creerCompteBancaire(String nom, double soldeInitial) {
        CompteBancaire compteBancaire = new CompteBancaire();
        compteBancaire.setSolde(soldeInitial);
        compteBancaire.setNom(nom);
        compteBancaireRepository.save(compteBancaire);
    }

    public TransactionDTO getTransactionById(Long id) {
        return transactionRepository.findById(id)
                .map(TransactionMapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Transaction non trouvée avec l'ID : " + id));
    }

    @Transactional
    public TransactionDTO creerTransaction(String type, double montant, String description, Long compteId) {
        CompteBancaire compte = compteBancaireRepository.findById(compteId)
                .orElseThrow(() -> new ResourceNotFoundException("Compte bancaire non trouvé"));

        Transaction transaction = new Transaction();
        transaction.setType(type);
        transaction.setMontant(montant);
        transaction.setDescription(description);
        transaction.setDate(LocalDate.now());
        transaction.setCompteBancaire(compte);

        // Mise à jour du solde du compte
        if ("VENTE".equals(type)) {
            compte.setSolde(compte.getSolde() + montant);
        } else if ("PRODUCTION".equals(type) || "ACHAT".equals(type)) {
            compte.setSolde(compte.getSolde() - montant);

            // Vérification du seuil d'alerte pour solde bas
            if (compte.getSolde() < 1000) {
                alerteService.creerAlerte("FINANCE", "WARNING",
                        "Solde bas sur le compte " + compte.getNom() + ": " + compte.getSolde() + " XAF");
            }
        }

        compteBancaireRepository.save(compte);
        Transaction saved = transactionRepository.save(transaction);
        return TransactionMapper.toDTO(saved);
    }

    public List<CompteBancaire> getAllComptesBancaires() {
        return compteBancaireRepository.findAll();
    }

    @Transactional
    public void transfertEntreCcomptes(Long sourceId, Long destinationId, double montant, String description) {
        CompteBancaire sourceCompte = compteBancaireRepository.findById(sourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Compte source non trouvé"));
        CompteBancaire destinationCompte = compteBancaireRepository.findById(destinationId)
                .orElseThrow(() -> new ResourceNotFoundException("Compte destination non trouvé"));

        if (sourceCompte.getSolde() < montant) {
            throw new IllegalArgumentException("Solde insuffisant pour effectuer le transfert");
        }

        sourceCompte.setSolde(sourceCompte.getSolde() - montant);
        destinationCompte.setSolde(destinationCompte.getSolde() + montant);

        compteBancaireRepository.save(sourceCompte);
        compteBancaireRepository.save(destinationCompte);

        // Enregistrer les transactions
        Transaction debit = new Transaction();
        debit.setType("TRANSFERT_DEBIT");
        debit.setMontant(montant);
        debit.setDescription("Transfert vers " + destinationCompte.getNom() + ": " + description);
        debit.setDate(LocalDate.now());
        debit.setCompteBancaire(sourceCompte);

        Transaction credit = new Transaction();
        credit.setType("TRANSFERT_CREDIT");
        credit.setMontant(montant);
        credit.setDescription("Transfert depuis " + sourceCompte.getNom() + ": " + description);
        credit.setDate(LocalDate.now());
        credit.setCompteBancaire(destinationCompte);

        transactionRepository.save(debit);
        transactionRepository.save(credit);
    }

    public double calculerDepensesSalariales() {
        Double total = transactionRepository.sumMontantByTypeIn(List.of("SALAIRE", "SALAIRE_ENFOIS"));
        return total != null ? total : 0.0;
    }
}