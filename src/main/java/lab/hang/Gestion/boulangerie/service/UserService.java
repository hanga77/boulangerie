package lab.hang.Gestion.boulangerie.service;


import lab.hang.Gestion.boulangerie.dto.RegisterRequest;
import lab.hang.Gestion.boulangerie.exception.UserNotAuthenticatedException;
import lab.hang.Gestion.boulangerie.exception.UserNotFoundException;
import lab.hang.Gestion.boulangerie.model.User;
import lab.hang.Gestion.boulangerie.repository.UserRepository;

import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private static final Set<String> VALID_ROLES = Set.of("ADMIN", "MANAGER", "BOULANGER");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;



    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void registerUser(RegisterRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        if (userRepository.count() == 0) {
            user.setRole("ADMIN");
            user.setActive(true);
        } else {
            user.setRole("BOULANGER");
            user.setActive(false);
        }
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
    }

    public boolean hasUsers() {
        return userRepository.count() > 0;
    }

    @Transactional
    public User activateUser(Long userId) {
        User user = getUserById(userId);
        user.setActive(true);
        return userRepository.save(user);
    }

    @Transactional
    public User updateUserRole(Long userId, String newRole) {
        if (!VALID_ROLES.contains(newRole)) {
            throw new IllegalArgumentException("Rôle invalide : " + newRole + ". Valeurs acceptées : " + VALID_ROLES);
        }
        User user = getUserById(userId);
        user.setRole(newRole);
        return userRepository.save(user);
    }

    public List<User> getAllInactiveUsers() {
        return userRepository.findByActiveIsFalse();
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    @Transactional
    public void changePassword(Long userId, String newPassword) {
        User user = getUserById(userId);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }


    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.debug("Récupération utilisateur courant - authentication: {}", authentication);

        if (authentication == null) {
            throw new UserNotAuthenticatedException("Aucune authentification trouvée.");
        }

        if (!authentication.isAuthenticated()) {
            throw new UserNotAuthenticatedException("Utilisateur non authentifié.");
        }

        String username = authentication.getName();
        log.debug("Utilisateur authentifié: {}", username);

        if ("anonymousUser".equals(username)) {
            throw new UserNotAuthenticatedException("Session utilisateur invalide.");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur non trouvé: " + username));

        if (user.getId() == null) {
            throw new UserNotFoundException("ID utilisateur manquant pour: " + username);
        }

        return user;
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur non trouvé avec l'ID : " + userId));
    }

}