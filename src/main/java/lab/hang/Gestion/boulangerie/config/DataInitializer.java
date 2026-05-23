package lab.hang.Gestion.boulangerie.config;

import lab.hang.Gestion.boulangerie.model.*;
import lab.hang.Gestion.boulangerie.model.FournisseurDette;
import lab.hang.Gestion.boulangerie.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final MatierePremiereRepository matierePremiereRepository;
    private final ProduitRepository produitRepository;
    private final CompteBancaireRepository compteBancaireRepository;
    private final ChargeFixeRepository chargeFixeRepository;
    private final FournisseurRepository fournisseurRepository;
    private final PointDeVenteRepository pointDeVenteRepository;
    private final CommandeRepository commandeRepository;
    private final StockMovementRepository stockMovementRepository;
    private final FournisseurDetteRepository fournisseurDetteRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           MatierePremiereRepository matierePremiereRepository,
                           ProduitRepository produitRepository,
                           CompteBancaireRepository compteBancaireRepository,
                           ChargeFixeRepository chargeFixeRepository,
                           FournisseurRepository fournisseurRepository,
                           PointDeVenteRepository pointDeVenteRepository,
                           CommandeRepository commandeRepository,
                           StockMovementRepository stockMovementRepository,
                           FournisseurDetteRepository fournisseurDetteRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.matierePremiereRepository = matierePremiereRepository;
        this.produitRepository = produitRepository;
        this.compteBancaireRepository = compteBancaireRepository;
        this.chargeFixeRepository = chargeFixeRepository;
        this.fournisseurRepository = fournisseurRepository;
        this.pointDeVenteRepository = pointDeVenteRepository;
        this.commandeRepository = commandeRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.fournisseurDetteRepository = fournisseurDetteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        initComptesBancaires();
        initUsers();
        List<MatierePremiere> matieres = initMatieresPremieres();
        initProduits(matieres);
        initChargesFixes();
        List<Fournisseur> fournisseurs = initFournisseurs();
        try {
            initDettes(fournisseurs);
        } catch (Exception e) {
            log.warn("Seed dettes ignoré: {}", e.getMessage());
        }
        List<PointDeVente> pdvs = initPointsDeVente();
        try {
            initCommandes(pdvs);
        } catch (Exception e) {
            log.warn("Seed commandes ignoré: {}", e.getMessage());
        }
        try {
            initStockMovements(matieres);
        } catch (Exception e) {
            log.warn("Seed mouvements ignoré: {}", e.getMessage());
        }
    }

    // ── Comptes bancaires ──────────────────────────────────────────────────

    private void initComptesBancaires() {
        if (compteBancaireRepository.count() > 0) return;

        CompteBancaire principal = new CompteBancaire();
        principal.setNom("Compte Principal");
        principal.setSolde(500_000.0);
        compteBancaireRepository.save(principal);

        CompteBancaire epargne = new CompteBancaire();
        epargne.setNom("Compte Épargne");
        epargne.setSolde(1_200_000.0);
        compteBancaireRepository.save(epargne);

        CompteBancaire fournisseurs = new CompteBancaire();
        fournisseurs.setNom("Compte Fournisseurs");
        fournisseurs.setSolde(0.0);
        compteBancaireRepository.save(fournisseurs);
    }

    // ── Utilisateurs ──────────────────────────────────────────────────────

    private void initUsers() {
        if (userRepository.count() > 0) return;

        userRepository.save(buildUser("xavier",    "admin123",   "ADMIN",     true));
        userRepository.save(buildUser("manager",   "manager123", "MANAGER",   true));
        userRepository.save(buildUser("boulanger1","pain2024",   "BOULANGER", true));
        userRepository.save(buildUser("boulanger2","pain2024",   "BOULANGER", true));
    }

    private User buildUser(String username, String password, String role, boolean active) {
        User u = new User();
        u.setUsername(username);
        u.setPassword(passwordEncoder.encode(password));
        u.setRole(role);
        u.setActive(active);
        return u;
    }

    // ── Matières premières ────────────────────────────────────────────────

    private List<MatierePremiere> initMatieresPremieres() {
        if (matierePremiereRepository.count() > 0)
            return matierePremiereRepository.findAll();

        return matierePremiereRepository.saveAll(List.of(
            mp("Farine de blé",  "kg",  150.0, 20.0,  350.0),
            mp("Sucre",          "kg",   80.0, 10.0,  600.0),
            mp("Levure",         "g",  2000.0, 500.0,   4.0),
            mp("Sel",            "g",  3000.0, 500.0,   0.5),
            mp("Beurre",         "g",  5000.0, 500.0,   8.0),
            mp("Lait",           "mL", 8000.0, 1000.0,  0.9),
            mp("Huile végétale", "mL", 3000.0, 500.0,   1.2),
            mp("Œufs",           "u",   120.0,  20.0, 150.0),
            mp("Cacao",          "g",  1500.0, 200.0,   5.0),
            mp("Levure chimique","g",   800.0, 100.0,   3.5)
        ));
    }

    private MatierePremiere mp(String nom, String unite, double stock,
                               double stockMin, double prix) {
        MatierePremiere m = new MatierePremiere();
        m.setNom(nom);
        m.setUniteMesure(unite);
        m.setStock(stock);
        m.setStockMinimum(stockMin);
        m.setPrixUnitaire(prix);
        return m;
    }

    // ── Produits avec recettes ────────────────────────────────────────────

    private void initProduits(List<MatierePremiere> matieres) {
        if (produitRepository.count() > 0) return;

        MatierePremiere farine = get(matieres, "Farine de blé");
        MatierePremiere sucre  = get(matieres, "Sucre");
        MatierePremiere levure = get(matieres, "Levure");
        MatierePremiere sel    = get(matieres, "Sel");
        MatierePremiere beurre = get(matieres, "Beurre");
        MatierePremiere lait   = get(matieres, "Lait");
        MatierePremiere huile  = get(matieres, "Huile végétale");
        MatierePremiere oeufs  = get(matieres, "Œufs");
        MatierePremiere cacao  = get(matieres, "Cacao");
        MatierePremiere levChi = get(matieres, "Levure chimique");

        produitRepository.saveAll(List.of(

            // Pain ordinaire — 100 XAF
            produit("Pain ordinaire", 100.0, 200, 0,
                Map.of(farine, 0.08, levure, 2.0, sel, 1.5, huile, 5.0)),

            // Baguette — 200 XAF
            produit("Baguette", 200.0, 150, 0,
                Map.of(farine, 0.2, levure, 3.0, sel, 3.0, beurre, 10.0)),

            // Croissant — 350 XAF
            produit("Croissant", 350.0, 80, 30,
                Map.of(farine, 0.05, beurre, 25.0, sucre, 8.0, levure, 2.0, lait, 20.0)),

            // Pain de mie — 800 XAF
            produit("Pain de mie", 800.0, 60, 20,
                Map.of(farine, 0.25, beurre, 30.0, sucre, 20.0, lait, 100.0, levure, 5.0, sel, 5.0)),

            // Brioche — 500 XAF
            produit("Brioche", 500.0, 50, 15,
                Map.of(farine, 0.1, beurre, 50.0, sucre, 30.0, oeufs, 1.0, levure, 3.0)),

            // Gâteau au chocolat — 3 500 XAF
            produit("Gâteau chocolat", 3500.0, 10, 0,
                Map.of(farine, 0.2, sucre, 0.15, beurre, 100.0, oeufs, 3.0,
                       lait, 100.0, cacao, 50.0, levChi, 5.0)),

            // Donut — 250 XAF
            produit("Donut", 250.0, 100, 40,
                Map.of(farine, 0.06, sucre, 15.0, oeufs, 1.0,
                       lait, 40.0, levure, 2.0, huile, 20.0))
        ));
    }

    private Produit produit(String nom, double prix, int qteAttendue,
                            int qteVenteLibre, Map<MatierePremiere, Double> recette) {
        Produit p = new Produit();
        p.setNom(nom);
        p.setPrix(prix);
        p.setQuantiteAttendue(qteAttendue);
        p.setQuantiteVenteLibreJournaliere(qteVenteLibre);
        p.setMatieresPremieres(recette);
        return p;
    }

    private MatierePremiere get(List<MatierePremiere> list, String nom) {
        return list.stream()
                .filter(m -> m.getNom().equals(nom))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Matière première introuvable : " + nom));
    }

    // ── Charges fixes ─────────────────────────────────────────────────────

    private void initChargesFixes() {
        if (chargeFixeRepository.count() > 0) return;

        LocalDate debut = LocalDate.now().withDayOfMonth(1);

        chargeFixeRepository.saveAll(List.of(
            charge("LOYER",        150_000, "Loyer local boulangerie",        debut.plusMonths(1), "MENSUEL",     false),
            charge("ELECTRICITE",   45_000, "Facture électricité ENEO",       debut.plusMonths(1), "MENSUEL",     false),
            charge("EAU",           15_000, "Facture eau CDE",                debut.plusMonths(1), "MENSUEL",     false),
            charge("SALAIRE",       80_000, "Salaire boulanger 1",            debut.plusMonths(1), "MENSUEL",     false),
            charge("SALAIRE",       80_000, "Salaire boulanger 2",            debut.plusMonths(1), "MENSUEL",     false),
            charge("SALAIRE",      120_000, "Salaire manager",                debut.plusMonths(1), "MENSUEL",     false),
            charge("ASSURANCE",     25_000, "Assurance locaux et équipements",debut.plusMonths(3), "TRIMESTRIEL", false),
            charge("MAINTENANCE",   30_000, "Entretien four et pétrin",       debut.plusMonths(6), "SEMESTRIEL",  false),
            charge("ELECTRICITE",   45_000, "Facture électricité — mois précédent", debut.minusDays(5), "MENSUEL", true),
            charge("EAU",           15_000, "Facture eau — mois précédent",   debut.minusDays(5), "MENSUEL",     true)
        ));
    }

    private ChargeFixe charge(String type, double montant, String desc,
                              LocalDate echeance, String periodicite, boolean paye) {
        ChargeFixe c = new ChargeFixe();
        c.setType(type);
        c.setMontant(montant);
        c.setDescription(desc);
        c.setDateEcheance(echeance);
        c.setPeriodicite(periodicite);
        c.setPaye(paye);
        return c;
    }

    // ── Fournisseurs ──────────────────────────────────────────────────────

    private List<Fournisseur> initFournisseurs() {
        if (fournisseurRepository.count() > 0) return fournisseurRepository.findAll();

        return fournisseurRepository.saveAll(List.of(
            fournisseur("SOSUCAM",        "Njombé, Littoral",       "+237 233 000 001",
                        "commercial@sosucam.cm",  "M. Mbarga Paul",   "Sucre, céréales"),
            fournisseur("Grands Moulins",  "Douala, Bonapriso",      "+237 233 421 100",
                        "ventes@gmcam.cm",        "Mme Ngo Epée",     "Farine, blé, semoule"),
            fournisseur("Laiterie du Berger", "Yaoundé, Mfandena",  "+237 222 310 045",
                        "contact@laiterie.cm",    "M. Djoumessi",     "Lait, beurre, produits laitiers"),
            fournisseur("PromoPack",       "Douala, Akwa",           "+237 699 123 456",
                        "info@promopack.cm",      "Mme Bello Awa",    "Emballages, sachets alimentaires"),
            fournisseur("AgroDistrib",     "Yaoundé, Melen",         "+237 677 654 321",
                        "achats@agrodistrib.cm",  "M. Nkolo",         "Œufs, farine, levure, épices")
        ));
    }

    private Fournisseur fournisseur(String nom, String adresse, String tel,
                                    String email, String contact, String categorie) {
        Fournisseur f = new Fournisseur();
        f.setNom(nom);
        f.setAdresse(adresse);
        f.setTelephone(tel);
        f.setEmail(email);
        f.setReferenceContact(contact);
        f.setCategorieProduits(categorie);
        return f;
    }

    // ── Dettes fournisseurs ───────────────────────────────────────────────

    private void initDettes(List<Fournisseur> fournisseurs) {
        if (fournisseurDetteRepository.count() > 0) return;
        if (fournisseurs.size() < 3) return;

        LocalDate today = LocalDate.now();
        Fournisseur sosucam  = getFournisseur(fournisseurs, "SOSUCAM");
        Fournisseur moulins  = getFournisseur(fournisseurs, "Grands Moulins");
        Fournisseur laiterie = getFournisseur(fournisseurs, "Laiterie du Berger");

        fournisseurDetteRepository.saveAll(List.of(
            dette(sosucam,  85_000, today.minusDays(20), today.plusDays(10),  "EN_COURS"),
            dette(sosucam,  30_000, today.minusDays(45), today.minusDays(5),  "EN_COURS"),
            dette(moulins, 120_000, today.minusDays(10), today.plusDays(20),  "EN_COURS"),
            dette(moulins,  50_000, today.minusDays(60), today.minusDays(30), "REMBOURSEE"),
            dette(laiterie, 65_000, today.minusDays(15), today.plusDays(15),  "EN_COURS")
        ));
    }

    private FournisseurDette dette(Fournisseur fournisseur, double montant,
                                   LocalDate creation, LocalDate echeance, String status) {
        FournisseurDette d = new FournisseurDette();
        d.setFournisseur(fournisseur);
        d.setMontantDette(montant);
        d.setDateCreation(creation);
        d.setDateEcheance(echeance);
        d.setStatus(status);
        return d;
    }

    private Fournisseur getFournisseur(List<Fournisseur> list, String nom) {
        return list.stream()
                .filter(f -> f.getNom().equals(nom))
                .findFirst()
                .orElse(list.get(0));
    }

    // ── Points de vente ───────────────────────────────────────────────────

    private List<PointDeVente> initPointsDeVente() {
        if (pointDeVenteRepository.count() > 0) return pointDeVenteRepository.findAll();

        return pointDeVenteRepository.saveAll(List.of(
            pdv("Boutique Centrale",   "Rue de la Paix, Yaoundé Centre",      TypePointDeVente.BOUTIQUE),
            pdv("Marché Mokolo",       "Marché Mokolo, Yaoundé",              TypePointDeVente.MARCHE),
            pdv("Marché Mfoundi",      "Marché central Mfoundi, Yaoundé",     TypePointDeVente.MARCHE),
            pdv("Dépôt Essos",         "Quartier Essos, Yaoundé",             TypePointDeVente.DEPOT),
            pdv("Guichet Boulangerie", "Boulangerie principale, Yaoundé",     TypePointDeVente.GUICHET_CENTRAL)
        ));
    }

    private PointDeVente pdv(String nom, String adresse, TypePointDeVente type) {
        PointDeVente p = new PointDeVente();
        p.setNom(nom);
        p.setAdresse(adresse);
        p.setType(type);
        p.setActif(true);
        return p;
    }

    // ── Commandes ─────────────────────────────────────────────────────────

    private void initCommandes(List<PointDeVente> pdvs) {
        if (commandeRepository.count() > 0) return;

        User manager = userRepository.findByUsername("manager")
                .orElseGet(() -> userRepository.findAll().get(0));

        List<Produit> produits = produitRepository.findAll();
        if (produits.isEmpty() || pdvs.isEmpty()) return;

        Produit pain    = getProduit(produits, "Pain ordinaire");
        Produit baguette = getProduit(produits, "Baguette");
        Produit croissant = getProduit(produits, "Croissant");
        Produit painMie = getProduit(produits, "Pain de mie");
        Produit donut   = getProduit(produits, "Donut");

        PointDeVente boutique = pdvs.get(0);
        PointDeVente mokolo   = pdvs.get(1);
        PointDeVente mfoundi  = pdvs.get(2);
        PointDeVente essos    = pdvs.get(3);

        LocalDate today = LocalDate.now();

        commandeRepository.saveAll(List.of(
            commande(today,          boutique, manager, Map.of(pain, 50, baguette, 30, croissant, 20), true),
            commande(today,          mokolo,   manager, Map.of(pain, 80, baguette, 40), true),
            commande(today,          mfoundi,  manager, Map.of(pain, 60, donut, 25), false),
            commande(today,          essos,    manager, Map.of(baguette, 20, painMie, 10), false),
            commande(today.minusDays(1), boutique, manager, Map.of(pain, 45, baguette, 25, croissant, 15), true),
            commande(today.minusDays(1), mokolo,   manager, Map.of(pain, 70, baguette, 35, donut, 20), true),
            commande(today.minusDays(2), boutique, manager, Map.of(pain, 55, baguette, 30, painMie, 8), true),
            commande(today.minusDays(2), mfoundi,  manager, Map.of(pain, 65, croissant, 10), true),
            commande(today.minusDays(3), boutique, manager, Map.of(pain, 60, baguette, 40, donut, 30), true),
            commande(today.minusDays(4), mokolo,   manager, Map.of(pain, 90, baguette, 50), true)
        ));
    }

    private Commande commande(LocalDate date, PointDeVente pdv, User user,
                               Map<Produit, Integer> produits, boolean processed) {
        Commande c = new Commande();
        c.setDateCommande(date);
        c.setPointDeVente(pdv);
        c.setUser(user);
        c.setProduitsCommandes(new HashMap<>(produits));
        c.setProcessed(processed);
        double total = produits.entrySet().stream()
                .mapToDouble(e -> e.getKey().getPrix() * e.getValue())
                .sum();
        c.setCoutTotal(total);
        return c;
    }

    private Produit getProduit(List<Produit> list, String nom) {
        return list.stream()
                .filter(p -> p.getNom().equals(nom))
                .findFirst()
                .orElse(list.get(0));
    }

    // ── Mouvements de stock ───────────────────────────────────────────────

    private void initStockMovements(List<MatierePremiere> matieres) {
        if (stockMovementRepository.count() > 0) return;

        User admin = userRepository.findByUsername("xavier")
                .orElseGet(() -> userRepository.findAll().get(0));

        MatierePremiere farine  = get(matieres, "Farine de blé");
        MatierePremiere sucre   = get(matieres, "Sucre");
        MatierePremiere levure  = get(matieres, "Levure");
        MatierePremiere beurre  = get(matieres, "Beurre");
        MatierePremiere lait    = get(matieres, "Lait");
        MatierePremiere oeufs   = get(matieres, "Œufs");
        MatierePremiere huile   = get(matieres, "Huile végétale");

        LocalDate today = LocalDate.now();

        stockMovementRepository.saveAll(List.of(
            // Entrées fournisseurs (J-7 à J-3)
            mouvement("ENTREE", 50.0,  farine, today.minusDays(7), admin, "Livraison Grands Moulins"),
            mouvement("ENTREE", 30.0,  sucre,  today.minusDays(7), admin, "Livraison SOSUCAM"),
            mouvement("ENTREE", 5000.0, levure, today.minusDays(6), admin, "Réapprovisionnement AgroDistrib"),
            mouvement("ENTREE", 10000.0, beurre, today.minusDays(5), admin, "Livraison Laiterie du Berger"),
            mouvement("ENTREE", 20000.0, lait,  today.minusDays(5), admin, "Livraison Laiterie du Berger"),
            mouvement("ENTREE", 50.0,  oeufs,  today.minusDays(4), admin, "Livraison AgroDistrib"),
            mouvement("ENTREE", 25.0,  farine, today.minusDays(3), admin, "Complément stock Grands Moulins"),
            mouvement("ENTREE", 5000.0, huile,  today.minusDays(3), admin, "Réapprovisionnement huile"),

            // Sorties production (J-4 à J-1)
            mouvement("SORTIE", 8.0,  farine, today.minusDays(4), admin, "Production pain/baguettes du jour"),
            mouvement("SORTIE", 1500.0, levure, today.minusDays(4), admin, "Production pain/baguettes du jour"),
            mouvement("SORTIE", 6.0,  farine, today.minusDays(3), admin, "Production pain ordinaire"),
            mouvement("SORTIE", 5.0,  sucre,  today.minusDays(2), admin, "Production croissants et donuts"),
            mouvement("SORTIE", 2000.0, beurre, today.minusDays(2), admin, "Production croissants"),
            mouvement("SORTIE", 10.0, farine, today.minusDays(1), admin, "Grande production du vendredi"),
            mouvement("SORTIE", 8.0,  sucre,  today.minusDays(1), admin, "Grande production du vendredi"),
            mouvement("SORTIE", 30.0, oeufs,  today.minusDays(1), admin, "Production brioches et gâteaux"),

            // Retours invendus
            mouvement("RETOUR", 2.0,  farine, today.minusDays(2), admin, "Retour surplus production"),
            mouvement("RETOUR", 500.0, levure, today.minusDays(1), admin, "Levure non utilisée retournée")
        ));
    }

    private StockMovement mouvement(String type, double quantite, MatierePremiere matiere,
                                     LocalDate date, User user, String motif) {
        StockMovement m = new StockMovement();
        m.setType(type);
        m.setQuantite(quantite);
        m.setMatierePremiere(matiere);
        m.setDate(date);
        m.setUser(user);
        m.setMotif(motif);
        return m;
    }
}
