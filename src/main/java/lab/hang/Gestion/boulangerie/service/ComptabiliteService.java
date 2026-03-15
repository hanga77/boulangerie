package lab.hang.Gestion.boulangerie.service;

import lab.hang.Gestion.boulangerie.model.CompteComptable;
import lab.hang.Gestion.boulangerie.model.EcritureComptable;
import lab.hang.Gestion.boulangerie.repository.EcritureComptableRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ComptabiliteService {

    private static final Logger log = LoggerFactory.getLogger(ComptabiliteService.class);

    private final EcritureComptableRepository ecritureRepository;

    @Autowired
    public ComptabiliteService(EcritureComptableRepository ecritureRepository) {
        this.ecritureRepository = ecritureRepository;
    }

    @Transactional
    public void enregistrerTransaction(String type, double montant, String description) {
        String compteDebit;
        String compteCredit;

        switch (type) {
            case "VENTE":
                compteDebit = CompteComptable.BANQUE.getCode();
                compteCredit = CompteComptable.VENTES_MARCHANDISES.getCode();
                break;
            case "ACHAT":
                compteDebit = CompteComptable.ACHATS_MARCHANDISES.getCode();
                compteCredit = CompteComptable.BANQUE.getCode();
                break;
            default:
                compteDebit = CompteComptable.COMPTE_ATTENTE.getCode();
                compteCredit = CompteComptable.COMPTE_ATTENTE.getCode();
        }

        String ref = "TX-" + System.currentTimeMillis();

        EcritureComptable debit = new EcritureComptable();
        debit.setDate(LocalDate.now());
        debit.setCompte(compteDebit);
        debit.setLibelle(description);
        debit.setDebit(montant);
        debit.setCredit(0);
        debit.setReference(ref + "-D");

        EcritureComptable credit = new EcritureComptable();
        credit.setDate(LocalDate.now());
        credit.setCompte(compteCredit);
        credit.setLibelle(description);
        credit.setDebit(0);
        credit.setCredit(montant);
        credit.setReference(ref + "-C");

        ecritureRepository.save(debit);
        ecritureRepository.save(credit);
        log.debug("Transaction enregistrée : type={}, montant={}", type, montant);
    }

    /**
     * Grand livre : toutes les écritures de la période, groupées par compte.
     */
    public Map<String, List<EcritureComptable>> genererGrandLivre(YearMonth periode) {
        LocalDate debut = periode.atDay(1);
        LocalDate fin = periode.atEndOfMonth();

        List<EcritureComptable> ecritures = ecritureRepository.findByDateBetweenOrderByDateAsc(debut, fin);

        return ecritures.stream()
                .collect(Collectors.groupingBy(
                        EcritureComptable::getCompte,
                        TreeMap::new,
                        Collectors.toList()
                ));
    }

    /**
     * Bilan simplifié au sens du plan comptable général (comptes 1-5).
     * Retourne : actif (5xx → banque, 3xx → stocks, 4xx créances),
     *            passif (1xx capitaux, 4xx dettes).
     */
    public Map<String, Double> genererBilan(LocalDate dateBilan) {
        LocalDate debut = LocalDate.of(dateBilan.getYear(), 1, 1);

        // Actif
        double banque = ecritureRepository.sumDebitByComptePrefix("5", debut, dateBilan)
                - ecritureRepository.sumCreditByComptePrefix("5", debut, dateBilan);

        double stocks = ecritureRepository.sumDebitByComptePrefix("3", debut, dateBilan)
                - ecritureRepository.sumCreditByComptePrefix("3", debut, dateBilan);

        double creances = ecritureRepository.sumDebitByComptePrefix("41", debut, dateBilan)
                - ecritureRepository.sumCreditByComptePrefix("41", debut, dateBilan);

        double actifTotal = Math.max(banque, 0) + Math.max(stocks, 0) + Math.max(creances, 0);

        // Passif
        double dettes = ecritureRepository.sumCreditByComptePrefix("40", debut, dateBilan)
                - ecritureRepository.sumDebitByComptePrefix("40", debut, dateBilan);

        double capitaux = ecritureRepository.sumCreditByComptePrefix("1", debut, dateBilan)
                - ecritureRepository.sumDebitByComptePrefix("1", debut, dateBilan);

        // Résultat de la période (intégré au passif)
        double produits = ecritureRepository.sumCreditByComptePrefix("7", debut, dateBilan)
                - ecritureRepository.sumDebitByComptePrefix("7", debut, dateBilan);
        double charges = ecritureRepository.sumDebitByComptePrefix("6", debut, dateBilan)
                - ecritureRepository.sumCreditByComptePrefix("6", debut, dateBilan);
        double resultat = produits - charges;

        double passifTotal = Math.max(dettes, 0) + Math.max(capitaux, 0) + resultat;

        Map<String, Double> bilan = new LinkedHashMap<>();
        bilan.put("banque", Math.max(banque, 0));
        bilan.put("stocks", Math.max(stocks, 0));
        bilan.put("creances", Math.max(creances, 0));
        bilan.put("actifTotal", actifTotal);
        bilan.put("dettes", Math.max(dettes, 0));
        bilan.put("capitaux", Math.max(capitaux, 0));
        bilan.put("resultat", resultat);
        bilan.put("passifTotal", passifTotal);
        return bilan;
    }

    /**
     * Compte de résultat pour la période donnée.
     * Produits (7xx) – Charges (6xx) = Résultat net.
     */
    public Map<String, Double> genererCompteResultat(LocalDate debut, LocalDate fin) {
        double chiffreAffaires = ecritureRepository.sumCreditByComptePrefix("707", debut, fin)
                - ecritureRepository.sumDebitByComptePrefix("707", debut, fin);

        double achats = ecritureRepository.sumDebitByComptePrefix("607", debut, fin)
                - ecritureRepository.sumCreditByComptePrefix("607", debut, fin);

        double autresCharges = ecritureRepository.sumDebitByComptePrefix("6", debut, fin)
                - ecritureRepository.sumCreditByComptePrefix("6", debut, fin)
                - Math.max(achats, 0);

        double autresProduits = ecritureRepository.sumCreditByComptePrefix("7", debut, fin)
                - ecritureRepository.sumDebitByComptePrefix("7", debut, fin)
                - Math.max(chiffreAffaires, 0);

        double totalProduits = Math.max(chiffreAffaires, 0) + Math.max(autresProduits, 0);
        double totalCharges = Math.max(achats, 0) + Math.max(autresCharges, 0);
        double resultatNet = totalProduits - totalCharges;

        Map<String, Double> cr = new LinkedHashMap<>();
        cr.put("chiffreAffaires", Math.max(chiffreAffaires, 0));
        cr.put("autresProduits", Math.max(autresProduits, 0));
        cr.put("totalProduits", totalProduits);
        cr.put("achats", Math.max(achats, 0));
        cr.put("autresCharges", Math.max(autresCharges, 0));
        cr.put("totalCharges", totalCharges);
        cr.put("resultatNet", resultatNet);
        return cr;
    }

    /**
     * Clôture mensuelle : crée une écriture de résumé pour le mois
     * et remet les comptes de résultat à zéro.
     */
    @Transactional
    public void effectuerClotureMensuelle(YearMonth mois) {
        LocalDate debut = mois.atDay(1);
        LocalDate fin = mois.atEndOfMonth();

        Map<String, Double> cr = genererCompteResultat(debut, fin);
        double resultat = cr.getOrDefault("resultatNet", 0.0);

        String ref = "CLOTURE-" + mois.toString() + "-" + System.currentTimeMillis();

        // Écriture de clôture du résultat vers le compte de résultat (120)
        EcritureComptable ecritureCloture = new EcritureComptable();
        ecritureCloture.setDate(fin);
        ecritureCloture.setReference(ref);
        ecritureCloture.setLibelle("Clôture mensuelle " + mois);
        ecritureCloture.setCompte("120"); // Résultat de l'exercice

        if (resultat >= 0) {
            ecritureCloture.setCredit(resultat);
            ecritureCloture.setDebit(0);
        } else {
            ecritureCloture.setDebit(Math.abs(resultat));
            ecritureCloture.setCredit(0);
        }

        ecritureRepository.save(ecritureCloture);
        log.info("Clôture mensuelle effectuée pour {} — résultat : {} XAF", mois, resultat);
    }
}
