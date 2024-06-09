package lab.hang.boulangerie.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "admin_profile")
public class AdminProfile {
    @Id
    private  Long userAccountId;
    @OneToOne
    @JoinColumn(name = "user_account_id")
    @MapsId
    private  Users userId;
    private String firstName;
    private String lastname;
    private String poste;

    @Column( length = 120)
    private String profilePhoto;

    public AdminProfile(Users users) {
        this.userId = users;
    }
}
