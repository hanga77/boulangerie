package lab.hang.Gestion.boulangerie.repository;


import lab.hang.Gestion.boulangerie.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    List<User> findByActiveIsFalse();
    long count();
}