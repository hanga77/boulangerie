package lab.hang.boulangerie.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.engine.internal.Cascade;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "users")
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;
    @Column(nullable = false, unique = true)
    private String  email;
    @Column(nullable = false)
    private String password;
    private boolean isActive;
    @DateTimeFormat(pattern = "dd-MM-YYYY")
    private Date registrationDate;

    @ManyToOne(cascade=CascadeType.ALL)
    @JoinColumn(name = "usersTypeId", referencedColumnName = "usersTypeId")
    private UsersType usersTypeId;

    @OneToMany(targetEntity= Session.class,mappedBy = "usersId", cascade = CascadeType.ALL)
    private List<Session> sessions;

    @OneToMany(targetEntity= MouvementMatiere.class,mappedBy = "usersId", cascade = CascadeType.ALL)
    private List<MouvementMatiere> matierePremiereList;
}
