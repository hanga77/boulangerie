package lab.hang.Gestion.boulangerie.dto;

import java.time.LocalDate;
import java.util.Map;

public class VenteLibreDTO {

    private Long id;
    private LocalDate dateVente;
    private Long productionId;
    private String vendeurUsername;
    private Map<Long, Integer> produitsVendus;   // produitId -> quantite
    private Map<Long, String> nomsProduits;       // produitId -> nom (pour affichage)
    private Map<Long, Double> prixProduits;       // produitId -> prix unitaire
    private double montantTotal;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getDateVente() { return dateVente; }
    public void setDateVente(LocalDate dateVente) { this.dateVente = dateVente; }

    public Long getProductionId() { return productionId; }
    public void setProductionId(Long productionId) { this.productionId = productionId; }

    public String getVendeurUsername() { return vendeurUsername; }
    public void setVendeurUsername(String vendeurUsername) { this.vendeurUsername = vendeurUsername; }

    public Map<Long, Integer> getProduitsVendus() { return produitsVendus; }
    public void setProduitsVendus(Map<Long, Integer> produitsVendus) { this.produitsVendus = produitsVendus; }

    public Map<Long, String> getNomsProduits() { return nomsProduits; }
    public void setNomsProduits(Map<Long, String> nomsProduits) { this.nomsProduits = nomsProduits; }

    public Map<Long, Double> getPrixProduits() { return prixProduits; }
    public void setPrixProduits(Map<Long, Double> prixProduits) { this.prixProduits = prixProduits; }

    public double getMontantTotal() { return montantTotal; }
    public void setMontantTotal(double montantTotal) { this.montantTotal = montantTotal; }
}
