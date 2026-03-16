package lab.hang.Gestion.boulangerie.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lab.hang.Gestion.boulangerie.model.TypePointDeVente;

import java.util.List;

public class PointDeVenteDTO {

    private Long id;

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    private String adresse;

    @NotNull(message = "Le type est obligatoire")
    private TypePointDeVente type;

    private boolean actif;

    private List<GuichetDTO> guichets;

    // Stats (calculées)
    private double chiffreAffairesTotal;
    private long nombreVentes;
    private double chiffreAffairesMois;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public TypePointDeVente getType() { return type; }
    public void setType(TypePointDeVente type) { this.type = type; }

    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }

    public List<GuichetDTO> getGuichets() { return guichets; }
    public void setGuichets(List<GuichetDTO> guichets) { this.guichets = guichets; }

    public double getChiffreAffairesTotal() { return chiffreAffairesTotal; }
    public void setChiffreAffairesTotal(double chiffreAffairesTotal) { this.chiffreAffairesTotal = chiffreAffairesTotal; }

    public long getNombreVentes() { return nombreVentes; }
    public void setNombreVentes(long nombreVentes) { this.nombreVentes = nombreVentes; }

    public double getChiffreAffairesMois() { return chiffreAffairesMois; }
    public void setChiffreAffairesMois(double chiffreAffairesMois) { this.chiffreAffairesMois = chiffreAffairesMois; }
}
