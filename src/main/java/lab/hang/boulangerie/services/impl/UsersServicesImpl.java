package lab.hang.boulangerie.services.impl;


import lab.hang.boulangerie.dto.UsersDTO;
import lab.hang.boulangerie.entity.AdminProfile;
import lab.hang.boulangerie.entity.Users;
import lab.hang.boulangerie.entity.UsersProfile;
import lab.hang.boulangerie.repository.AdminProfileRepository;
import lab.hang.boulangerie.repository.UsersProfileRepository;
import lab.hang.boulangerie.repository.UsersRepository;
import lab.hang.boulangerie.services.UsersServices;

import lombok.AllArgsConstructor;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@AllArgsConstructor
public class UsersServicesImpl implements UsersServices {
    private UsersRepository repository;
    private AdminProfileRepository adminProfileRepository;
    private UsersProfileRepository usersProfileRepository;
    private PasswordEncoder passwordEncoder;

    @Override
    public Users addNew(UsersDTO usersDTO){
        usersDTO.setActive(true);
        usersDTO.setRegistrationDate(new Date(System.currentTimeMillis()));
        Users users = repository.save(mapToUsers(usersDTO));
        Long UserTypeId = usersDTO.getUsersTypeId().getUsersTypeId();
        if (UserTypeId == 1L) {
            adminProfileRepository.save(new AdminProfile(users));
        }else{
            usersProfileRepository.save(new UsersProfile(users));
        }
        return users;
    }

    @Override
    public Object getCurrentUsersProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String username = authentication.getName();
            Users user = repository.findByEmail(username).orElseThrow(()-> new UsernameNotFoundException("Could not be found User"));
            Long userId = user.getUserId();
            if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("Admin"))){
                return adminProfileRepository.findById(userId);
            }else {
                return usersProfileRepository.findById(userId).orElse(new UsersProfile());
            }
        }
        return null;
    }

    private Users mapToUsers(UsersDTO usersDTO){
        Users users = new Users();
        users.setUserId(usersDTO.getUserId());
        users.setPassword(passwordEncoder.encode(usersDTO.getPassword()));
        users.setEmail(usersDTO.getEmail());
        users.setActive(usersDTO.isActive());
        users.setRegistrationDate(usersDTO.getRegistrationDate());
        users.setUsersTypeId(usersDTO.getUsersTypeId());
        return users;
    }
    private UsersDTO mapToUsersDTO(Users usersDTO){
        UsersDTO users = new UsersDTO();
        users.setUserId(usersDTO.getUserId());
        users.setPassword(usersDTO.getPassword());
        users.setEmail(usersDTO.getEmail());
        users.setActive(usersDTO.isActive());
        users.setRegistrationDate(usersDTO.getRegistrationDate());
        users.setUsersTypeId(usersDTO.getUsersTypeId());
        return users;
    }
}
