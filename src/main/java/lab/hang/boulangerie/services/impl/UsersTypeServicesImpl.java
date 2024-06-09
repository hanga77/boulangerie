package lab.hang.boulangerie.services.impl;


import lab.hang.boulangerie.dto.UsersTypeDTO;
import lab.hang.boulangerie.entity.UsersType;
import lab.hang.boulangerie.repository.UsersTypeRepository;
import lab.hang.boulangerie.services.UsersTypeServices;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class UsersTypeServicesImpl implements UsersTypeServices {
    private UsersTypeRepository usersTypeRepository;

    @Override
    public List<UsersTypeDTO> getAll(){
        return usersTypeRepository.findAll().stream().map(this::mapTOUsersTypeDTO).collect(Collectors.toList());
    }

    private UsersTypeDTO mapTOUsersTypeDTO(UsersType usersType){
        UsersTypeDTO usersTypeDTO = new UsersTypeDTO();
        usersTypeDTO.setUsersTypeId(usersType.getUsersTypeId());
        usersTypeDTO.setUsersTypeName(usersType.getUsersTypeName());
        usersTypeDTO.setUsers(usersType.getUsers());
        return usersTypeDTO;
    }
}
