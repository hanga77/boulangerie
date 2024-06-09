package lab.hang.boulangerie.dto;



import lab.hang.boulangerie.entity.Users;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UsersProfileDTO {
    private  Long userAccountId;
    private Users userId;
    private String firstName;
    private String lastName;
    private String profilePhoto;
}
