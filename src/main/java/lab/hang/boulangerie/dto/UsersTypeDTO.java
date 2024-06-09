package lab.hang.boulangerie.dto;


import lab.hang.boulangerie.entity.Users;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UsersTypeDTO {

    private Long usersTypeId;

    private String  usersTypeName;

    private List<Users> users;
}
