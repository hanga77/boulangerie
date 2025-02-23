package lab.hang.Gestion.boulangerie.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Entity
public class Commande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate dateCommande;

    @ElementCollection
    @CollectionTable(name = "commande_produit", joinColumns = @JoinColumn(name = "commande_id"))
    @MapKeyJoinColumn(name = "produit_id")
    @Column(name = "quantite")
    private Map<Produit, Integer> produitsCommandes = new HashMap<>();


    @Column(nullable = false)
    private String pointDeVente; // Ou une relation @ManyToOne avec une entité PointDeVente

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Relation avec l'utilisateur

    private Double coutTotal;

    @Column(nullable = false)
    private boolean processed = false;

    @ManyToOne
    @JoinColumn(name = "production_id")
    private Production production;

    // Ajouter les getters et setters
    public Production getProduction() {
        return production;
    }

    public void setProduction(Production production) {
        this.production = production;
    }

    // Getters, setters, constructeurs


    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDateCommande() {
        return dateCommande;
    }

    public void setDateCommande(LocalDate dateCommande) {
        this.dateCommande = dateCommande;
    }

    public Map<Produit, Integer> getProduitsCommandes() {
        return produitsCommandes;
    }

    public void setProduitsCommandes(Map<Produit, Integer> produitsCommandes) {
        this.produitsCommandes = produitsCommandes;
    }

    public String getPointDeVente() {
        return pointDeVente;
    }

    public void setPointDeVente(String pointDeVente) {
        this.pointDeVente = pointDeVente;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Double getCoutTotal() {
        return coutTotal;
    }

    public void setCoutTotal(Double coutTotal) {
        this.coutTotal = coutTotal;
    }
}