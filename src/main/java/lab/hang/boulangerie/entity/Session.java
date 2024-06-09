package lab.hang.boulangerie.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "session_travail")
public class Session {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sessionID;
    @Column(unique = true,nullable = false)
    private String hashCode;

    @ManyToOne(cascade=CascadeType.ALL)
    @JoinColumn(name = "users_id")
    @MapsId
    private Users usersId;
    @CreationTimestamp
    private LocalDateTime dateCreated;
}
