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
@Table(name = "usersTypes")
public class UsersType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long usersTypeId;

    private String  usersTypeName;

    @OneToMany(targetEntity = Users.class, mappedBy = "usersTypeId", cascade = CascadeType.ALL)
    private List<Users> users;

}
