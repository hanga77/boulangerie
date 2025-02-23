package lab.hang.Gestion.boulangerie.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.Map;

@Entity

public class Livraison {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate dateLivraison;

    @Column(nullable = false)
    private String nomClient;

    @ManyToOne
    @JoinColumn(name = "production_id", nullable = false)
    private Production production;

    @ElementCollection
    @CollectionTable(name = "livraison_produit",
            joinColumns = @JoinColumn(name = "livraison_id"))
    private Map<Produit, ProduitLivre> produitsLivres;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private double montantTotal;

    // Getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDateLivraison() {
        return dateLivraison;
    }

    public void setDateLivraison(LocalDate dateLivraison) {
        this.dateLivraison = dateLivraison;
    }

    public String getNomClient() {
        return nomClient;
    }

    public void setNomClient(String nomClient) {
        this.nomClient = nomClient;
    }

    public Production getProduction() {
        return production;
    }

    public void setProduction(Production production) {
        this.production = production;
    }

    public Map<Produit, ProduitLivre> getProduitsLivres() {
        return produitsLivres;
    }

    public void setProduitsLivres(Map<Produit, ProduitLivre> produitsLivres) {
        this.produitsLivres = produitsLivres;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public double getMontantTotal() {
        return montantTotal;
    }

    public void setMontantTotal(double montantTotal) {
        this.montantTotal = montantTotal;
    }
}