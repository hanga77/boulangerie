package lab.hang.Gestion.boulangerie.service;

import lab.hang.Gestion.boulangerie.exception.ResourceNotFoundException;
import lab.hang.Gestion.boulangerie.model.Employe;
import lab.hang.Gestion.boulangerie.model.User;
import lab.hang.Gestion.boulangerie.repository.EmployeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class EmployeService {

    private final EmployeRepository employeRepository;

    public EmployeService(EmployeRepository employeRepository) {
        this.employeRepository = employeRepository;
    }

    public List<Employe> getAllActifs() {
        return employeRepository.findByActifTrue();
    }

    public List<Employe> getAll() {
        return employeRepository.findAll();
    }

    public Employe getById(Long id) {
        return employeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Employé introuvable : " + id));
    }

    @Transactional
    public Employe save(Employe employe) {
        return employeRepository.save(employe);
    }

    @Transactional
    public void desactiver(Long id) {
        Employe employe = getById(id);
        employe.setActif(false);
        employeRepository.save(employe);
    }

    public Optional<Employe> findByUser(User user) {
        return employeRepository.findByUser(user);
    }
}
