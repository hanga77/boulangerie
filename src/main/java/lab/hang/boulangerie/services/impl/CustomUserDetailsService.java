package lab.hang.boulangerie.services.impl;


import lab.hang.boulangerie.entity.Users;
import lab.hang.boulangerie.repository.UsersRepository;
import lab.hang.boulangerie.util.CustomUserDetails;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private UsersRepository usersRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
       Users user = usersRepository.findByEmail(username).orElseThrow(()-> new UsernameNotFoundException("Could not be found User"));
        return new CustomUserDetails(user);
    }
}
