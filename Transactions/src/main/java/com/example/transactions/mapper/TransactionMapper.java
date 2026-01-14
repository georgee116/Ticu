package com.example.transactions.mapper;

import com.example.transactions.dto.request.PostTransactionDto;
import com.example.transactions.dto.request.PutTransactionDto;
import com.example.transactions.dto.response.TransactionDto;
import com.example.transactions.entity.Transaction;
import com.example.transactions.enums.TransactionStatus;

import java.time.LocalDateTime;

public class TransactionMapper {
    public static Transaction ToEntity(PostTransactionDto transactionDto) {
        return Transaction.builder()
                .amount(transactionDto.getAmount())
                .currency(transactionDto.getCurrency())
                .transactionType(transactionDto.getTransactionType())
                .status(TransactionStatus.PENDING)
                .description(transactionDto.getDescription())
                .fromAccountId(transactionDto.getFromAccountId()) /// relations not established yet
                .toAccountId(transactionDto.getToAccountId())
                .initiatedAt(LocalDateTime.now())
                .build();
    }

    public static Transaction ToEntity(PutTransactionDto transactionDto, Long id, String transactionId) {
        return Transaction.builder()
                .id(id)
                .transactionId(transactionId)
                .amount(transactionDto.getAmount())
                .currency(transactionDto.getCurrency())
                .transactionType(transactionDto.getTransactionType())
                .status(transactionDto.getStatus())
                .description(transactionDto.getDescription())
                .fromAccountId(transactionDto.getFromAccountId()) /// relations not established yet
                .toAccountId(transactionDto.getToAccountId())
                .build();
    }

    public static TransactionDto ToDto(Transaction transaction) {
        return new TransactionDto(
                transaction.getToAccountId(),
                transaction.getTransactionId(),
                transaction.getFromAccountId(),
                transaction.getFromAccountNumber(),
                transaction.getToAccountNumber(),
                transaction.getTransactionType(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getDescription(),
                transaction.getStatus(),
                transaction.getFailureReason()
        );
    }
}
