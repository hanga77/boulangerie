package lab.hang.Gestion.boulangerie.dto;

import java.time.LocalDate;
import java.util.*;

public class ProductionDTO {
    private Long id;
    private LocalDate dateProduction;
    private Map<Long, Double> matieresPremieresUtilisees = new HashMap<>();
    private Map<Long, Integer> produitsProduits = new HashMap<>();
    private Map<Long, Double> quantitesReellesUtilisees = new HashMap<>();
    private Map<Long, Integer> produitsRestants = new HashMap<>();
    private Long userId;
    private Set<Long> commandeIds = new HashSet<>();

    // Getters et Setters
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Map<Long, Double> getMatieresPremieresUtilisees() {
        return matieresPremieresUtilisees;
    }

    public void setMatieresPremieresUtilisees(Map<Long, Double> matieresPremieresUtilisees) {
        this.matieresPremieresUtilisees = matieresPremieresUtilisees;
    }

    public Map<Long, Integer> getProduitsProduits() {
        return produitsProduits;
    }

    public void setProduitsProduits(Map<Long, Integer> produitsProduits) {
        this.produitsProduits = produitsProduits;
    }

    public Map<Long, Double> getQuantitesReellesUtilisees() {
        return quantitesReellesUtilisees;
    }

    public void setQuantitesReellesUtilisees(Map<Long, Double> quantitesReellesUtilisees) {
        this.quantitesReellesUtilisees = quantitesReellesUtilisees;
    }

    public Map<Long, Integer> getProduitsRestants() {
        return produitsRestants;
    }

    public void setProduitsRestants(Map<Long, Integer> produitsRestants) {
        this.produitsRestants = produitsRestants;
    }

    public Set<Long> getCommandeIds() { return commandeIds; }

    public void setCommandeIds(Set<Long> commandeIds) { this.commandeIds = commandeIds; }
}