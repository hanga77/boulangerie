package lab.hang.boulangerie.repository;

import lab.hang.boulangerie.entity.Session;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface SessionRepository extends JpaRepository<Session, Long> {

    Page<Session> findAll(Pageable pageable);

}
