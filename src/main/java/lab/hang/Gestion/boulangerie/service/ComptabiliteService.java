package lab.hang.Gestion.boulangerie.service;

import lab.hang.Gestion.boulangerie.model.CompteBancaire;
import lab.hang.Gestion.boulangerie.model.EcritureComptable;
import lab.hang.Gestion.boulangerie.model.Facture;
import lab.hang.Gestion.boulangerie.repository.CompteBancaireRepository;
import lab.hang.Gestion.boulangerie.repository.EcritureComptableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@Service
public class ComptabiliteService {
    private final EcritureComptableRepository ecritureRepository;
    private final CompteBancaireRepository compteRepository;

    @Autowired
    public ComptabiliteService(EcritureComptableRepository ecritureRepository,
                               CompteBancaireRepository compteRepository) {
        this.ecritureRepository = ecritureRepository;
        this.compteRepository = compteRepository;
    }

    @Transactional
    public void enregistrerTransaction(String type, double montant, String description) {
        // Déterminer les comptes à débiter et créditer selon le type de transaction
        String compteDebit;
        String compteCredit;

        switch (type) {
            case "VENTE":
                compteDebit = "512"; // Banque
                compteCredit = "707"; // Ventes de marchandises
                break;
            case "ACHAT":
                compteDebit = "607"; // Achats de marchandises
                compteCredit = "512"; // Banque
                break;
            // Autres cas
            default:
                compteDebit = "471"; // Compte d'attente
                compteCredit = "471"; // Compte d'attente
        }

        // Créer l'écriture de débit
        EcritureComptable debit = new EcritureComptable();
        debit.setDate(LocalDate.now());
        debit.setCompte(compteDebit);
        debit.setLibelle(description);
        debit.setDebit(montant);
        debit.setCredit(0);
        debit.setReference("TX-" + System.currentTimeMillis());

        // Créer l'écriture de crédit
        EcritureComptable credit = new EcritureComptable();
        credit.setDate(LocalDate.now());
        credit.setCompte(compteCredit);
        credit.setLibelle(description);
        credit.setDebit(0);
        credit.setCredit(montant);
        credit.setReference("TX-" + System.currentTimeMillis());

        // Enregistrer les écritures
        ecritureRepository.save(debit);
        ecritureRepository.save(credit);
    }

    // Générer le grand livre
    public Map<String, List<EcritureComptable>> genererGrandLivre(YearMonth periode) {
        // Implémentation
        return null;
    }

    // Générer le bilan
    public Map<String, Double> genererBilan(LocalDate dateBilan) {
        // Implémentation
        return null;
    }

    // Générer le compte de résultat
    public Map<String, Double> genererCompteResultat(LocalDate debut, LocalDate fin) {
        // Implémentation
        return null;
    }

    // Clôture mensuelle
    @Transactional
    public void effectuerClotureMensuelle(YearMonth mois) {
        // Implémentation
    }
}