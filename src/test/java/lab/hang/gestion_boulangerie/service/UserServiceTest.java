package lab.hang.gestion_boulangerie.service;

import lab.hang.Gestion.boulangerie.dto.RegisterRequest;
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

    // ── registerUser ───────────────────────────────────────────────────────

    @Test
    void registerUser_premier_utilisateur_est_admin_et_actif() {
        when(userRepository.count()).thenReturn(0L);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RegisterRequest request = new RegisterRequest();
        request.setUsername("admin");
        request.setPassword("password123");

        userService.registerUser(request);

        verify(userRepository).save(argThat(u ->
                "ADMIN".equals(u.getRole()) && u.isActive()));
    }

    @Test
    void registerUser_utilisateur_suivant_est_boulanger_et_inactif() {
        when(userRepository.count()).thenReturn(5L);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RegisterRequest request = new RegisterRequest();
        request.setUsername("jean");
        request.setPassword("password123");

        userService.registerUser(request);

        verify(userRepository).save(argThat(u ->
                "BOULANGER".equals(u.getRole()) && !u.isActive()));
    }

    @Test
    void registerUser_role_admin_demande_est_retrograde_en_boulanger() {
        // /register est accessible anonymement : un attaquant ne doit pas pouvoir
        // s'auto-attribuer ADMIN via un POST forgé (role n'est pas dans le formulaire
        // mais rien n'empêche un client HTTP direct de l'envoyer).
        when(userRepository.count()).thenReturn(5L);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RegisterRequest request = new RegisterRequest();
        request.setUsername("attaquant");
        request.setPassword("password123");
        request.setRole("ADMIN");

        userService.registerUser(request);

        verify(userRepository).save(argThat(u -> "BOULANGER".equals(u.getRole())));
    }

    @Test
    void registerUser_role_manager_demande_est_retrograde_en_boulanger() {
        when(userRepository.count()).thenReturn(5L);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RegisterRequest request = new RegisterRequest();
        request.setUsername("jean");
        request.setPassword("password123");
        request.setRole("MANAGER");

        userService.registerUser(request);

        verify(userRepository).save(argThat(u -> "BOULANGER".equals(u.getRole())));
    }

    @Test
    void registerUser_role_sans_privilege_est_accepte() {
        when(userRepository.count()).thenReturn(5L);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RegisterRequest request = new RegisterRequest();
        request.setUsername("caissier1");
        request.setPassword("password123");
        request.setRole("CAISSIER");

        userService.registerUser(request);

        verify(userRepository).save(argThat(u -> "CAISSIER".equals(u.getRole())));
    }

    @Test
    void registerUser_encode_le_mot_de_passe() {
        when(userRepository.count()).thenReturn(0L);
        when(passwordEncoder.encode("mdpClair")).thenReturn("mdpHashé");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RegisterRequest request = new RegisterRequest();
        request.setUsername("user");
        request.setPassword("mdpClair");

        userService.registerUser(request);

        verify(userRepository).save(argThat(u -> "mdpHashé".equals(u.getPassword())));
    }

    // ── activateUser ───────────────────────────────────────────────────────

    @Test
    void activateUser_met_active_a_true() {
        User user = new User();
        user.setId(1L);
        user.setActive(false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.activateUser(1L);

        assertThat(result.isActive()).isTrue();
    }

    // ── updateUserRole ─────────────────────────────────────────────────────

    @Test
    void updateUserRole_modifie_le_role() {
        User user = new User();
        user.setId(1L);
        user.setRole("BOULANGER");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.updateUserRole(1L, "MANAGER");

        assertThat(result.getRole()).isEqualTo("MANAGER");
    }

    @Test
    void updateUserRole_role_invalide_leve_IllegalArgumentException() {
        assertThatThrownBy(() -> userService.updateUserRole(1L, "SUPER_ADMIN"))
                .isInstanceOf(java.lang.IllegalArgumentException.class)
                .hasMessageContaining("SUPER_ADMIN");
    }

    @Test
    void updateUserRole_roles_valides_acceptes() {
        User user = new User();
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThatNoException().isThrownBy(() -> userService.updateUserRole(1L, "ADMIN"));
        assertThatNoException().isThrownBy(() -> userService.updateUserRole(1L, "MANAGER"));
        assertThatNoException().isThrownBy(() -> userService.updateUserRole(1L, "BOULANGER"));
    }

    // ── getUserById ────────────────────────────────────────────────────────

    @Test
    void getUserById_id_inexistant_leve_UserNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("99");
    }
}
