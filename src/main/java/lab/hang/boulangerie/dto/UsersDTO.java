package lab.hang.boulangerie.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lab.hang.boulangerie.entity.UsersType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UsersDTO {

    private Long userId;
    @NotEmpty
    @Email
    private String  email;
    @NotEmpty
    private String password;
    private String confirmPassword;
    private boolean isActive;
    @DateTimeFormat(pattern = "dd-MM-YYYY")
    private Date registrationDate;
    private UsersType usersTypeId;
}
