package lab.hang.Gestion.boulangerie.service;

import lab.hang.Gestion.boulangerie.dto.TransactionDTO;
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

    public FinanceService(TransactionRepository transactionRepository, CompteBancaireRepository compteBancaireRepository) {
        this.transactionRepository = transactionRepository;
        this.compteBancaireRepository = compteBancaireRepository;
    }

    public double calculerProfit(LocalDate startDate, LocalDate endDate) {
        List<Transaction> transactions = transactionRepository.findByDateBetween(startDate, endDate);
        double profit = 0;

        for (Transaction transaction : transactions) {
            if (transaction.getType().equals("VENTE")) {
                profit += transaction.getMontant(); // Revenus des ventes
            } else if (transaction.getType().equals("PRODUCTION") || transaction.getType().equals("ACHAT")) {
                profit -= transaction.getMontant(); // Dépenses de production et achats
            }
        }

        return profit;
    }

    public CompteBancaire getCompteBancaire(Long id) {
        return compteBancaireRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Compte bancaire non trouvé avec l'ID : " + id));
    }

    public List<Transaction> getTransactionsByDateRange(LocalDate startDate, LocalDate endDate) {
        return transactionRepository.findByDateBetween(startDate, endDate);
    }

    public double calculerCoutsProduction() {
        List<Transaction> transactions = transactionRepository.findByType("PRODUCTION");
        double coutsProduction = 0;

        for (Transaction transaction : transactions) {
            coutsProduction += transaction.getMontant();
        }

        return coutsProduction;
    }

    public double calculerRevenusVentes() {
        List<Transaction> transactions = transactionRepository.findByType("VENTE");
        double revenusVentes = 0;

        for (Transaction transaction : transactions) {
            revenusVentes += transaction.getMontant();
        }

        return revenusVentes;
    }

    public double calculerProfitTotal() {
        double coutsProduction = calculerCoutsProduction();
        double revenusVentes = calculerRevenusVentes();
        return coutsProduction - revenusVentes; // Profit total = couts production - revenus ventes
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
        List<Transaction> transactions = transactionRepository.findByTypeIn(List.of("PRODUCTION", "ACHAT"));
        return transactions.stream()
                .mapToDouble(Transaction::getMontant)
                .sum();
    }

    public double calculerProfit() {
        double revenus = calculerRevenusTotaux();
        double depenses = calculerDepensesTotales();
        return revenus - depenses;
    }

    public CompteBancaire getCompteBancairePrincipal() {
        return compteBancaireRepository.findByNom("Compte Principal");
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

    /*public double calculerCoutsProduction() {
        Double total = transactionRepository.sumMontantByType("PRODUCTION");
        return total != null ? total : 0.0; // Gérer le cas où total est null
    }*/
}