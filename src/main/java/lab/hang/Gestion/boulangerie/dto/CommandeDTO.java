package lab.hang.Gestion.boulangerie.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.Map;

public class CommandeDTO {

    private Long id;

    @NotNull(message = "La date de commande est obligatoire")
    private LocalDate dateCommande;

    @NotBlank(message = "Le point de vente est obligatoire")
    private String pointDeVente;

    @NotNull(message = "Les produits commandés sont obligatoires")
    @NotEmpty(message = "Au moins un produit doit être commandé")
    private Map<Long, Integer> produitsCommandes;

    private double coutTotal;
    private Long userId;
    private Map<Long, String> nomsProduits;
    private boolean processed;
    private Long productionId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public boolean isProcessed() { return processed; }
    public void setProcessed(boolean processed) { this.processed = processed; }

    public double getCoutTotal() { return coutTotal; }
    public void setCoutTotal(double coutTotal) { this.coutTotal = coutTotal; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public LocalDate getDateCommande() { return dateCommande; }
    public void setDateCommande(LocalDate dateCommande) { this.dateCommande = dateCommande; }

    public String getPointDeVente() { return pointDeVente; }
    public void setPointDeVente(String pointDeVente) { this.pointDeVente = pointDeVente; }

    public Map<Long, Integer> getProduitsCommandes() { return produitsCommandes; }
    public void setProduitsCommandes(Map<Long, Integer> produitsCommandes) { this.produitsCommandes = produitsCommandes; }

    public Map<Long, String> getNomsProduits() { return nomsProduits; }
    public void setNomsProduits(Map<Long, String> nomsProduits) { this.nomsProduits = nomsProduits; }

    public Long getProductionId() { return productionId; }
    public void setProductionId(Long productionId) { this.productionId = productionId; }
}
