package lab.hang.gestion_boulangerie.service;

import lab.hang.Gestion.boulangerie.exception.ResourceNotFoundException;
import lab.hang.Gestion.boulangerie.model.ChargeFixe;
import lab.hang.Gestion.boulangerie.model.CompteBancaire;
import lab.hang.Gestion.boulangerie.model.Transaction;
import lab.hang.Gestion.boulangerie.repository.ChargeFixeRepository;
import lab.hang.Gestion.boulangerie.repository.CompteBancaireRepository;
import lab.hang.Gestion.boulangerie.repository.TransactionRepository;
import lab.hang.Gestion.boulangerie.service.AlerteService;
import lab.hang.Gestion.boulangerie.service.ChargeFixeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChargeFixeServiceTest {

    @Mock ChargeFixeRepository chargeFixeRepository;
    @Mock TransactionRepository transactionRepository;
    @Mock CompteBancaireRepository compteBancaireRepository;
    @Mock AlerteService alerteService;
    @InjectMocks ChargeFixeService chargeFixeService;

    private ChargeFixe charge(boolean paye) {
        ChargeFixe c = new ChargeFixe();
        c.setId(1L);
        c.setType("LOYER");
        c.setDescription("Loyer local boulangerie");
        c.setMontant(150_000.0);
        c.setDateEcheance(LocalDate.of(2026, 6, 1));
        c.setPaye(paye);
        return c;
    }

    @Test
    void payerChargeFixe_createsTransactionAndUpdatesCharge() {
        ChargeFixe c = charge(false);
        CompteBancaire compte = new CompteBancaire();
        compte.setId(2L);
        compte.setNom("Compte Principal");
        compte.setSolde(500_000.0);

        when(chargeFixeRepository.findById(1L)).thenReturn(Optional.of(c));
        when(compteBancaireRepository.findById(2L)).thenReturn(Optional.of(compte));
        when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(chargeFixeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        chargeFixeService.payerChargeFixe(1L, 2L);

        assertThat(c.isPaye()).isTrue();
        assertThat(c.getDatePaiement()).isEqualTo(LocalDate.now());
        assertThat(c.getTransaction()).isNotNull();
        assertThat(compte.getSolde()).isCloseTo(350_000.0, within(0.01));

        ArgumentCaptor<Transaction> txCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(txCaptor.capture());
        Transaction tx = txCaptor.getValue();
        assertThat(tx.getType()).isEqualTo("CHARGE");
        assertThat(tx.getMontant()).isCloseTo(150_000.0, within(0.01));
        assertThat(tx.getDescription()).contains("LOYER").contains("Loyer local boulangerie");
        assertThat(tx.getCompteBancaire()).isEqualTo(compte);
    }

    @Test
    void payerChargeFixe_throwsWhenAlreadyPaid() {
        when(chargeFixeRepository.findById(1L)).thenReturn(Optional.of(charge(true)));

        assertThatThrownBy(() -> chargeFixeService.payerChargeFixe(1L, 2L))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("déjà payée");
    }

    @Test
    void payerChargeFixe_throwsWhenCompteNotFound() {
        when(chargeFixeRepository.findById(1L)).thenReturn(Optional.of(charge(false)));
        when(compteBancaireRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chargeFixeService.payerChargeFixe(1L, 99L))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getById_throwsWhenNotFound() {
        when(chargeFixeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chargeFixeService.getById(99L))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void payerChargeFixe_createsNextEcheanceWhenPeriodique() {
        ChargeFixe c = charge(false);
        c.setPeriodicite("MENSUEL");
        CompteBancaire compte = new CompteBancaire();
        compte.setId(2L);
        compte.setNom("Compte Principal");
        compte.setSolde(500_000.0);

        when(chargeFixeRepository.findById(1L)).thenReturn(Optional.of(c));
        when(compteBancaireRepository.findById(2L)).thenReturn(Optional.of(compte));
        when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(chargeFixeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        chargeFixeService.payerChargeFixe(1L, 2L);

        // Deux saves : la charge originale + la nouvelle échéance
        verify(chargeFixeRepository, times(2)).save(any(ChargeFixe.class));
        verify(alerteService).creerAlerte(eq("CHARGE_FIXE"), eq("INFO"), anyString());
    }

    @Test
    void payerChargeFixe_throwsWhenSoldeInsuffisant() {
        ChargeFixe c = charge(false);
        CompteBancaire compte = new CompteBancaire();
        compte.setId(2L);
        compte.setNom("Compte Principal");
        compte.setSolde(50_000.0); // Moins que le montant 150_000

        when(chargeFixeRepository.findById(1L)).thenReturn(Optional.of(c));
        when(compteBancaireRepository.findById(2L)).thenReturn(Optional.of(compte));

        assertThatThrownBy(() -> chargeFixeService.payerChargeFixe(1L, 2L))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Solde insuffisant");
    }
}
