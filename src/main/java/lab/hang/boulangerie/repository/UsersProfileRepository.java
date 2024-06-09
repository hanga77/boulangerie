package lab.hang.boulangerie.repository;



import lab.hang.boulangerie.entity.UsersProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsersProfileRepository extends JpaRepository<UsersProfile, Long> {
}
