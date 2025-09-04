package lab.hang.Gestion.boulangerie.repository;

import lab.hang.Gestion.boulangerie.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository  extends JpaRepository<Notification, Long> {
}
