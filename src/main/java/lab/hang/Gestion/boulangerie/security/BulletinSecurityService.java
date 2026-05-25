package lab.hang.Gestion.boulangerie.security;

import lab.hang.Gestion.boulangerie.repository.BulletinDePaieRepository;
import lab.hang.Gestion.boulangerie.repository.EmployeRepository;
import lab.hang.Gestion.boulangerie.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("bulletinSecurity")
public class BulletinSecurityService {

    private final BulletinDePaieRepository bulletinRepository;
    private final EmployeRepository employeRepository;
    private final UserRepository userRepository;

    public BulletinSecurityService(BulletinDePaieRepository bulletinRepository,
                                   EmployeRepository employeRepository,
                                   UserRepository userRepository) {
        this.bulletinRepository = bulletinRepository;
        this.employeRepository  = employeRepository;
        this.userRepository     = userRepository;
    }

    public boolean isBulletinOwner(Long bulletinId, Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
            .flatMap(employeRepository::findByUser)
            .map(employe -> bulletinRepository.findById(bulletinId)
                .map(b -> b.getEmploye().getId().equals(employe.getId()))
                .orElse(false))
            .orElse(false);
    }
}
