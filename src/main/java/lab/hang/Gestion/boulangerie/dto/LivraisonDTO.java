package lab.hang.Gestion.boulangerie.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.Map;


public class LivraisonDTO {
    private Long id;
    private LocalDate dateLivraison;
    private String nomClient;
    private Long productionId;
    private String livreurUsername;
    private Map<Long, ProduitLivreDTO> produitsLivres;
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

    public Long getProductionId() {
        return productionId;
    }

    public void setProductionId(Long productionId) {
        this.productionId = productionId;
    }

    public String getLivreurUsername() {
        return livreurUsername;
    }

    public void setLivreurUsername(String livreurUsername) {
        this.livreurUsername = livreurUsername;
    }

    public Map<Long, ProduitLivreDTO> getProduitsLivres() {
        return produitsLivres;
    }

    public void setProduitsLivres(Map<Long, ProduitLivreDTO> produitsLivres) {
        this.produitsLivres = produitsLivres;
    }

    public double getMontantTotal() {
        return montantTotal;
    }

    public void setMontantTotal(double montantTotal) {
        this.montantTotal = montantTotal;
    }
}
