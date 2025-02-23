package lab.hang.Gestion.boulangerie.dto;

import lombok.Data;

import java.util.Map;


public class CreateLivraisonRequest {
    private String nomClient;
    private Long productionId;
    private Map<Long, ProduitLivreRequest> produits;

    // Getters and setters


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

    public Map<Long, ProduitLivreRequest> getProduits() {
        return produits;
    }

    public void setProduits(Map<Long, ProduitLivreRequest> produits) {
        this.produits = produits;
    }
}
