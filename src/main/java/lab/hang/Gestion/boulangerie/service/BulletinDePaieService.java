package lab.hang.Gestion.boulangerie.service;

import lab.hang.Gestion.boulangerie.exception.ResourceNotFoundException;
import lab.hang.Gestion.boulangerie.model.*;
import lab.hang.Gestion.boulangerie.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class BulletinDePaieService {

    private static final double TAUX_CNPS_EMPLOYE  = 0.042;
    private static final double TAUX_CNPS_PATRONAL = 0.162;

    private final BulletinDePaieRepository bulletinRepository;
    private final EmployeRepository employeRepository;
    private final TransactionRepository transactionRepository;
    private final CompteBancaireRepository compteBancaireRepository;

    public BulletinDePaieService(BulletinDePaieRepository bulletinRepository,
                                  EmployeRepository employeRepository,
                                  TransactionRepository transactionRepository,
                                  CompteBancaireRepository compteBancaireRepository) {
        this.bulletinRepository = bulletinRepository;
        this.employeRepository  = employeRepository;
        this.transactionRepository = transactionRepository;
        this.compteBancaireRepository = compteBancaireRepository;
    }

    @Transactional
    public BulletinDePaie genererBulletin(Long employeId, LocalDate periode,
                                          double primes, double indemnitesTransport,
                                          double avanceSurSalaire) {
        Employe employe = employeRepository.findById(employeId)
            .orElseThrow(() -> new ResourceNotFoundException("Employé introuvable : " + employeId));

        if (bulletinRepository.findByEmployeAndPeriode(employe, periode).isPresent()) {
            throw new IllegalStateException(
                "Un bulletin existe déjà pour " + employe.getNom() + " sur la période " + periode);
        }

        double salaireBase  = employe.getSalaireBase();
        double salaireBrut  = salaireBase + primes + indemnitesTransport;
        double cnpsEmploye  = salaireBrut * TAUX_CNPS_EMPLOYE;
        double cnpsPatronal = salaireBrut * TAUX_CNPS_PATRONAL;
        double salaireNet   = salaireBrut - cnpsEmploye - avanceSurSalaire;

        if (salaireNet < 0) {
            throw new IllegalStateException(
                "L'avance sur salaire dépasse le net calculé. Bulletin non généré.");
        }

        BulletinDePaie bulletin = new BulletinDePaie();
        bulletin.setEmploye(employe);
        bulletin.setPeriode(periode);
        bulletin.setSalaireBase(salaireBase);
        bulletin.setPrimes(primes);
        bulletin.setIndemnitesTransport(indemnitesTransport);
        bulletin.setSalaireBrut(salaireBrut);
        bulletin.setCnpsEmploye(cnpsEmploye);
        bulletin.setCnpsPatronal(cnpsPatronal);
        bulletin.setAvanceSurSalaire(avanceSurSalaire);
        bulletin.setSalaireNet(salaireNet);
        bulletin.setStatut(StatutBulletin.GENERE);
        bulletin.setDateGeneration(LocalDate.now());

        return bulletinRepository.save(bulletin);
    }

    @Transactional
    public BulletinDePaie payerBulletin(Long bulletinId) {
        BulletinDePaie bulletin = bulletinRepository.findById(bulletinId)
            .orElseThrow(() -> new ResourceNotFoundException("Bulletin introuvable : " + bulletinId));

        if (bulletin.getStatut() == StatutBulletin.PAYE) {
            throw new IllegalStateException("Ce bulletin a déjà été payé.");
        }

        CompteBancaire compte = compteBancaireRepository.findByNom("Compte Principal")
            .orElseThrow(() -> new ResourceNotFoundException("Compte Principal introuvable"));

        if (compte.getSolde() < bulletin.getSalaireNet()) {
            throw new IllegalStateException(
                "Solde insuffisant : " + compte.getSolde() + " XAF disponibles, "
                + bulletin.getSalaireNet() + " XAF requis.");
        }

        Employe employe = bulletin.getEmploye();
        String moisAnnee = bulletin.getPeriode()
            .getMonth().getDisplayName(TextStyle.FULL, Locale.FRENCH)
            + " " + bulletin.getPeriode().getYear();

        Transaction transaction = new Transaction();
        transaction.setType("SALAIRE");
        transaction.setMontant(bulletin.getSalaireNet());
        transaction.setDate(LocalDate.now());
        transaction.setDescription("Salaire " + employe.getPrenom() + " " + employe.getNom()
            + " — " + moisAnnee);
        transaction.setCompteBancaire(compte);

        compte.setSolde(compte.getSolde() - bulletin.getSalaireNet());
        compteBancaireRepository.save(compte);
        Transaction savedTx = transactionRepository.save(transaction);

        bulletin.setStatut(StatutBulletin.PAYE);
        bulletin.setDatePaiement(LocalDate.now());
        bulletin.setTransaction(savedTx);

        return bulletinRepository.save(bulletin);
    }

    public BulletinDePaie getById(Long id) {
        return bulletinRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Bulletin introuvable : " + id));
    }

    public List<BulletinDePaie> getBulletinsParEmploye(Employe employe) {
        return bulletinRepository.findByEmploye(employe);
    }

    public List<BulletinDePaie> getBulletinsFiltres(LocalDate debut, LocalDate fin, StatutBulletin statut) {
        List<BulletinDePaie> bulletins = bulletinRepository.findByPeriodeBetween(debut, fin);
        if (statut != null) {
            return bulletins.stream().filter(b -> b.getStatut() == statut).collect(Collectors.toList());
        }
        return bulletins;
    }

    public List<BulletinDePaie> getByStatut(StatutBulletin statut) {
        return bulletinRepository.findByStatut(statut);
    }

    public double sumNetByStatut(StatutBulletin statut, LocalDate debut, LocalDate fin) {
        return bulletinRepository.findByPeriodeBetween(debut, fin).stream()
            .filter(b -> b.getStatut() == statut)
            .mapToDouble(BulletinDePaie::getSalaireNet)
            .sum();
    }
}
