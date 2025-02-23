package lab.hang.Gestion.boulangerie.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.util.Date;

@Entity
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Pattern(regexp = "^(ENTREE|SORTIE)$")
    private String type; // "ENTREE" ou "SORTIE"

    @Column(nullable = false)
    @Positive // Vérifiez que le stock est positif
    private double quantite;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne
    @JoinColumn(name = "matiere_premiere_id", nullable = false)
    private MatierePremiere matierePremiere;

    @ManyToOne
    private User user; // Qui a effectué le mouvement

    private String motif; // Raison du mouvement


    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getMotif() {
        return motif;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }



    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getQuantite() {
        return quantite;
    }

    public void setQuantite(double quantite) {
        this.quantite = quantite;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public MatierePremiere getMatierePremiere() {
        return matierePremiere;
    }

    public void setMatierePremiere(MatierePremiere matierePremiere) {
        this.matierePremiere = matierePremiere;
    }
}