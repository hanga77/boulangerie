package lab.hang.Gestion.boulangerie.repository;

import lab.hang.Gestion.boulangerie.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository  extends JpaRepository<Notification, Long> {
    List<Notification> findTop8ByOrderByDateCreationDesc();

    long countByVueFalse();
}
