package lab.hang.gestion_boulangerie.service;

import lab.hang.Gestion.boulangerie.model.*;
import lab.hang.Gestion.boulangerie.repository.*;
import lab.hang.Gestion.boulangerie.service.BulletinDePaieService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BulletinDePaieServiceTest {

    @Mock BulletinDePaieRepository bulletinRepository;
    @Mock EmployeRepository employeRepository;
    @Mock TransactionRepository transactionRepository;
    @Mock CompteBancaireRepository compteBancaireRepository;
    @InjectMocks BulletinDePaieService bulletinService;

    private Employe employe(Long id, double salaireBase) {
        Employe e = new Employe();
        e.setId(id);
        e.setNom("Dupont");
        e.setPrenom("Jean");
        e.setSalaireBase(salaireBase);
        return e;
    }

    @Test
    void genererBulletin_calculatesCorrectAmounts() {
        Employe e = employe(1L, 80_000.0);
        LocalDate periode = LocalDate.of(2026, 5, 1);

        when(employeRepository.findById(1L)).thenReturn(Optional.of(e));
        when(bulletinRepository.findByEmployeAndPeriode(e, periode)).thenReturn(Optional.empty());
        when(bulletinRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        BulletinDePaie b = bulletinService.genererBulletin(1L, periode, 10_000.0, 5_000.0, 0.0);

        assertThat(b.getSalaireBrut()).isEqualTo(95_000.0);
        assertThat(b.getCnpsEmploye()).isCloseTo(3_990.0, within(0.01));   // 95000 * 4.2%
        assertThat(b.getCnpsPatronal()).isCloseTo(15_390.0, within(0.01)); // 95000 * 16.2%
        assertThat(b.getSalaireNet()).isCloseTo(91_010.0, within(0.01));   // 95000 - 3990
        assertThat(b.getStatut()).isEqualTo(StatutBulletin.GENERE);
    }

    @Test
    void genererBulletin_throwsWhenDuplicate() {
        Employe e = employe(1L, 80_000.0);
        LocalDate periode = LocalDate.of(2026, 5, 1);

        when(employeRepository.findById(1L)).thenReturn(Optional.of(e));
        when(bulletinRepository.findByEmployeAndPeriode(e, periode))
            .thenReturn(Optional.of(new BulletinDePaie()));

        assertThatThrownBy(() -> bulletinService.genererBulletin(1L, periode, 0, 0, 0))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("bulletin existe déjà");
    }

    @Test
    void payerBulletin_createsTransactionAndUpdatesStatut() {
        Employe e = employe(1L, 80_000.0);
        BulletinDePaie bulletin = new BulletinDePaie();
        bulletin.setId(10L);
        bulletin.setEmploye(e);
        bulletin.setPeriode(LocalDate.of(2026, 5, 1));
        bulletin.setSalaireNet(91_010.0);
        bulletin.setStatut(StatutBulletin.GENERE);

        CompteBancaire compte = new CompteBancaire();
        compte.setSolde(500_000.0);

        when(bulletinRepository.findById(10L)).thenReturn(Optional.of(bulletin));
        when(compteBancaireRepository.findByNom("Compte Principal")).thenReturn(Optional.of(compte));
        when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(bulletinRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        BulletinDePaie result = bulletinService.payerBulletin(10L);

        assertThat(result.getStatut()).isEqualTo(StatutBulletin.PAYE);
        assertThat(result.getDatePaiement()).isNotNull();
        assertThat(result.getTransaction()).isNotNull();
        assertThat(compte.getSolde()).isCloseTo(500_000.0 - 91_010.0, within(0.01));

        ArgumentCaptor<Transaction> txCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(txCaptor.capture());
        assertThat(txCaptor.getValue().getType()).isEqualTo("SALAIRE");
        assertThat(txCaptor.getValue().getMontant()).isCloseTo(91_010.0, within(0.01));
    }

    @Test
    void payerBulletin_throwsWhenAlreadyPaid() {
        BulletinDePaie bulletin = new BulletinDePaie();
        bulletin.setStatut(StatutBulletin.PAYE);
        when(bulletinRepository.findById(5L)).thenReturn(Optional.of(bulletin));

        assertThatThrownBy(() -> bulletinService.payerBulletin(5L))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("déjà été payé");
    }
}
