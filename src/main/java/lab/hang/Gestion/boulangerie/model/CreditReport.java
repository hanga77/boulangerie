package lab.hang.Gestion.boulangerie.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDate;
import java.util.List;


public class CreditReport {
    private Fournisseur fournisseur;
    private double detteTotale;
    private List<FournisseurDette> dettes;
    private LocalDate dateGeneration;


    // Getters and setters


    public Fournisseur getFournisseur() {
        return fournisseur;
    }

    public void setFournisseur(Fournisseur fournisseur) {
        this.fournisseur = fournisseur;
    }

    public double getDetteTotale() {
        return detteTotale;
    }

    public void setDetteTotale(double detteTotale) {
        this.detteTotale = detteTotale;
    }

    public List<FournisseurDette> getDettes() {
        return dettes;
    }

    public void setDettes(List<FournisseurDette> dettes) {
        this.dettes = dettes;
    }

    public LocalDate getDateGeneration() {
        return dateGeneration;
    }

    public void setDateGeneration(LocalDate dateGeneration) {
        this.dateGeneration = dateGeneration;
    }

}
