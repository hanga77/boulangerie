package lab.hang.Gestion.boulangerie.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class GuichetDTO {

    private Long id;

    @NotBlank(message = "Le nom du guichet est obligatoire")
    private String nom;

    private int numero;

    @NotNull(message = "Le point de vente est obligatoire")
    private Long pointDeVenteId;

    private String nomPointDeVente;

    private boolean actif;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public int getNumero() { return numero; }
    public void setNumero(int numero) { this.numero = numero; }

    public Long getPointDeVenteId() { return pointDeVenteId; }
    public void setPointDeVenteId(Long pointDeVenteId) { this.pointDeVenteId = pointDeVenteId; }

    public String getNomPointDeVente() { return nomPointDeVente; }
    public void setNomPointDeVente(String nomPointDeVente) { this.nomPointDeVente = nomPointDeVente; }

    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }
}
