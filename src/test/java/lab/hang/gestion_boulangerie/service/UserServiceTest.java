package lab.hang.gestion_boulangerie.service;

import lab.hang.Gestion.boulangerie.exception.UserNotFoundException;
import lab.hang.Gestion.boulangerie.model.User;
import lab.hang.Gestion.boulangerie.repository.UserRepository;
import lab.hang.Gestion.boulangerie.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void registerUser_premier_utilisateur_doit_etre_admin_et_actif() {
        when(userRepository.count()).thenReturn(0L);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        User user = new User();
        user.setPassword("plaintext");
        userService.registerUser(user);

        assertThat(user.getRole()).isEqualTo("ADMIN");
        assertThat(user.isActive()).isTrue();
        assertThat(user.getPassword()).isEqualTo("encoded");
    }

    @Test
    void registerUser_autres_utilisateurs_doivent_etre_boulanger_et_inactifs() {
        when(userRepository.count()).thenReturn(5L);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        User user = new User();
        user.setPassword("plaintext");
        userService.registerUser(user);

        assertThat(user.getRole()).isEqualTo("BOULANGER");
        assertThat(user.isActive()).isFalse();
    }

    @Test
    void activateUser_doit_mettre_active_a_true() {
        User user = new User();
        user.setId(1L);
        user.setActive(false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.activateUser(1L);

        assertThat(result.isActive()).isTrue();
    }

    @Test
    void getUserById_avec_id_inexistant_doit_lever_UserNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void updateUserRole_doit_modifier_le_role() {
        User user = new User();
        user.setId(1L);
        user.setRole("BOULANGER");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.updateUserRole(1L, "MANAGER");

        assertThat(result.getRole()).isEqualTo("MANAGER");
    }
}
