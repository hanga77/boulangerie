package lab.hang.Gestion.boulangerie.dto;

import java.util.Map;
import lab.hang.Gestion.boulangerie.model.MoyenPaiement;

public class CreateVenteLibreRequest {

    private Long productionId;
    private Map<Long, Integer> produits; // produitId -> quantite vendue
    private Long guichetId;
    private MoyenPaiement moyenPaiement;

    public Long getProductionId() { return productionId; }
    public void setProductionId(Long productionId) { this.productionId = productionId; }

    public Map<Long, Integer> getProduits() { return produits; }
    public void setProduits(Map<Long, Integer> produits) { this.produits = produits; }

    public Long getGuichetId() { return guichetId; }
    public void setGuichetId(Long guichetId) { this.guichetId = guichetId; }

    public MoyenPaiement getMoyenPaiement() { return moyenPaiement; }
    public void setMoyenPaiement(MoyenPaiement moyenPaiement) { this.moyenPaiement = moyenPaiement; }
}

