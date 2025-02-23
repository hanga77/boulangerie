package lab.hang.Gestion.boulangerie.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class FournisseurDette {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "fournisseur_id")
    private Fournisseur fournisseur;

    private double montantDette;
    private LocalDate dateCreation;
    private LocalDate dateEcheance;
    private String status; // "EN_COURS", "REMBOURSEE"

    // Getters and setters


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Fournisseur getFournisseur() {
        return fournisseur;
    }

    public void setFournisseur(Fournisseur fournisseur) {
        this.fournisseur = fournisseur;
    }

    public double getMontantDette() {
        return montantDette;
    }

    public void setMontantDette(double montantDette) {
        this.montantDette = montantDette;
    }

    public LocalDate getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDate dateCreation) {
        this.dateCreation = dateCreation;
    }

    public LocalDate getDateEcheance() {
        return dateEcheance;
    }

    public void setDateEcheance(LocalDate dateEcheance) {
        this.dateEcheance = dateEcheance;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
