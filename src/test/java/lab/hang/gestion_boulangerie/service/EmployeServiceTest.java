package lab.hang.gestion_boulangerie.service;

import lab.hang.Gestion.boulangerie.exception.ResourceNotFoundException;
import lab.hang.Gestion.boulangerie.model.Employe;
import lab.hang.Gestion.boulangerie.model.User;
import lab.hang.Gestion.boulangerie.repository.EmployeRepository;
import lab.hang.Gestion.boulangerie.service.EmployeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeServiceTest {

    @Mock EmployeRepository employeRepository;
    @InjectMocks EmployeService employeService;

    @Test
    void getAllActifs_returnsOnlyActifs() {
        Employe e = new Employe();
        e.setActif(true);
        when(employeRepository.findByActifTrue()).thenReturn(List.of(e));

        List<Employe> result = employeService.getAllActifs();

        assertThat(result).hasSize(1);
        verify(employeRepository).findByActifTrue();
    }

    @Test
    void getById_throwsWhenNotFound() {
        when(employeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeService.getById(99L))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void desactiver_setsActifFalse() {
        Employe e = new Employe();
        e.setActif(true);
        when(employeRepository.findById(1L)).thenReturn(Optional.of(e));

        employeService.desactiver(1L);

        assertThat(e.isActif()).isFalse();
        verify(employeRepository).save(e);
    }

    @Test
    void findByUser_delegatesToRepository() {
        User user = new User();
        Employe e = new Employe();
        when(employeRepository.findByUser(user)).thenReturn(Optional.of(e));

        Optional<Employe> result = employeService.findByUser(user);

        assertThat(result).contains(e);
    }
}
