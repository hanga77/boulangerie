package lab.hang.Gestion.boulangerie.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class Fournisseur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String adresse;
    private String telephone;
    private String email;

    @OneToMany(mappedBy = "fournisseur")
    private List<FournisseurDette> dettes;

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<FournisseurDette> getDettes() {
        return dettes;
    }

    public void setDettes(List<FournisseurDette> dettes) {
        this.dettes = dettes;
    }
}