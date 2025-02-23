package lab.hang.Gestion.boulangerie.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity
public class Production {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate dateProduction;
    @ElementCollection
    @CollectionTable(name = "production_matiere_premiere", joinColumns = @JoinColumn(name = "production_id"))
    @MapKeyJoinColumn(name = "matiere_premiere_id")
    @Column(name = "quantite_utilisee")
    private Map<MatierePremiere, Double> matieresPremieresUtilisees = new HashMap<>();  // Initialize here

    @ElementCollection
    @CollectionTable(name = "production_quantites_reelles", joinColumns = @JoinColumn(name = "production_id"))
    @MapKeyJoinColumn(name = "matiere_premiere_id")
    @Column(name = "quantite_reelle")
    private Map<MatierePremiere, Double> quantitesReellesUtilisees = new HashMap<>();   // Initialize here

    @ElementCollection
    @CollectionTable(name = "production_produit", joinColumns = @JoinColumn(name = "production_id"))
    @MapKeyJoinColumn(name = "produit_id")
    @Column(name = "quantite_produite")
    private Map<Produit, Integer> produitsProduits = new HashMap<>();

    @ElementCollection
    @CollectionTable(name = "production_produits_restants", joinColumns = @JoinColumn(name = "production_id"))
    @MapKeyJoinColumn(name = "produit_id")
    @Column(name = "quantite_restante")
    private Map<Produit, Integer> produitsRestants = new HashMap<>();



    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // L'utilisateur qui a effectué la production

    private double QuantiteCommandee;

    @OneToMany(mappedBy = "production")
    private Set<Commande> commandes = new HashSet<>();

    public Set<Commande> getCommandes() {
        return commandes;
    }

    public void setCommandes(Set<Commande> commandes) {
        this.commandes = commandes;
    }

    public double getQuantiteCommandee() {
        return QuantiteCommandee;
    }

    public void setQuantiteCommandee(double QuantiteCommandee) {
        this.QuantiteCommandee = QuantiteCommandee;
    }

    // Getters et setters
    public Map<Produit, Integer> getProduitsRestants() { return produitsRestants; }
    public void setProduitsRestants(Map<Produit, Integer> produitsRestants) { this.produitsRestants = produitsRestants; }

    public Map<MatierePremiere, Double> getQuantitesReellesUtilisees() {
        return quantitesReellesUtilisees;
    }

    public void setQuantitesReellesUtilisees(Map<MatierePremiere, Double> quantitesReellesUtilisees) {
        this.quantitesReellesUtilisees = quantitesReellesUtilisees;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDateProduction() {
        return dateProduction;
    }

    public void setDateProduction(LocalDate dateProduction) {
        this.dateProduction = dateProduction;
    }

    public Map<MatierePremiere, Double> getMatieresPremieresUtilisees() {
        return matieresPremieresUtilisees;
    }

    public void setMatieresPremieresUtilisees(Map<MatierePremiere, Double> matieresPremieresUtilisees) {
        this.matieresPremieresUtilisees = matieresPremieresUtilisees;
    }

    public Map<Produit, Integer> getProduitsProduits() {
        return produitsProduits;
    }

    public void setProduitsProduits(Map<Produit, Integer> produitsProduits) {
        this.produitsProduits = produitsProduits;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}