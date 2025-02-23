package lab.hang.Gestion.boulangerie.mapper;

import lab.hang.Gestion.boulangerie.dto.TransactionDTO;
import lab.hang.Gestion.boulangerie.model.Transaction;

public class TransactionMapper {

    public static TransactionDTO toDTO(Transaction transaction) {
        TransactionDTO dto = new TransactionDTO();
        dto.setDate(transaction.getDate());
        dto.setType(transaction.getType());
        dto.setDescription(transaction.getDescription());
        dto.setMontant(transaction.getMontant());
        return dto;
    }

    public static Transaction toEntity(TransactionDTO dto) {
        Transaction transaction = new Transaction();
        transaction.setDate(dto.getDate());
        transaction.setType(dto.getType());
        transaction.setDescription(dto.getDescription());
        transaction.setMontant(dto.getMontant());
        return transaction;
    }
}

