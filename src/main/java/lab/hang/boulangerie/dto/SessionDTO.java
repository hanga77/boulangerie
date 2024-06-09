package lab.hang.boulangerie.dto;

import jakarta.persistence.*;
import lab.hang.boulangerie.entity.Users;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SessionDTO {
    private Long sessionID;
    private String hashCode;
    private Users usersId;
    @CreationTimestamp
    @DateTimeFormat(pattern = "dd-MM-YYYY")
    private LocalDateTime dateCreated;
}
