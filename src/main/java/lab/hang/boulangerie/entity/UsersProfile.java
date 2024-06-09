package lab.hang.boulangerie.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "users_profile")
public class UsersProfile {

    @Id
    private Long userAccountId;
    @OneToOne
    @JoinColumn(name = "user_account_id")
    @MapsId
    private  Users userId;
    private String firstName;
    private String lastName;
    private String state;
    @Column( length = 120)
    private String profilePhoto;

    public UsersProfile(Users users) {
        this.userId = users;
    }
}
