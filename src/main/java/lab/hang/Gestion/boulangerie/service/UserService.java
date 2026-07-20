package lab.hang.Gestion.boulangerie.service;


import lab.hang.Gestion.boulangerie.dto.RegisterRequest;
import lab.hang.Gestion.boulangerie.exception.ResourceNotFoundException;
import lab.hang.Gestion.boulangerie.exception.UserNotAuthenticatedException;
import lab.hang.Gestion.boulangerie.exception.UserNotFoundException;
import lab.hang.Gestion.boulangerie.model.PointDeVente;
import lab.hang.Gestion.boulangerie.model.User;
import lab.hang.Gestion.boulangerie.repository.PointDeVenteRepository;
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

    private static final Set<String> VALID_ROLES = Set.of(
            "ADMIN", "MANAGER", "BOULANGER", "MAGASINIER", "CAISSIER", "POINT_DE_VENTE");

    // /register est accessible anonymement (voir UserController) : n'accepter ici que des rôles
    // sans privilège d'administration, pour empêcher un attaquant de s'auto-attribuer ADMIN/MANAGER
    // via un POST forgé, en attendant qu'un admin active le compte sans y prêter attention.
    private static final Set<String> SELF_REGISTERABLE_ROLES = Set.of(
            "BOULANGER", "MAGASINIER", "CAISSIER", "POINT_DE_VENTE");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PointDeVenteRepository pointDeVenteRepository;



    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       PointDeVenteRepository pointDeVenteRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.pointDeVenteRepository = pointDeVenteRepository;
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
            String role = request.getRole();
            user.setRole(role != null && SELF_REGISTERABLE_ROLES.contains(role) ? role : "BOULANGER");
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

    @Transactional
    public User updatePointDeVente(Long userId, Long pointDeVenteId) {
        User user = getUserById(userId);
        if (pointDeVenteId == null) {
            user.setPointDeVente(null);
        } else {
            PointDeVente pointDeVente = pointDeVenteRepository.findById(pointDeVenteId)
                    .orElseThrow(() -> new ResourceNotFoundException("Point de vente non trouvé"));
            user.setPointDeVente(pointDeVente);
        }
        return userRepository.save(user);
    }

    public List<User> getAllInactiveUsers() {
        return userRepository.findByActiveIsFalse();
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void deleteUser(Long userId) {
        User user = getUserById(userId);
        if ("ADMIN".equals(user.getRole()) && userRepository.countByRole("ADMIN") <= 1) {
            throw new IllegalStateException("Impossible de supprimer le dernier administrateur.");
        }
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