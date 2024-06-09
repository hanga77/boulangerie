package lab.hang.boulangerie.services;


import lab.hang.boulangerie.dto.UsersDTO;
import lab.hang.boulangerie.entity.Users;

public interface UsersServices {
    Users addNew(UsersDTO usersDTO);

    Object getCurrentUsersProfile();
}
