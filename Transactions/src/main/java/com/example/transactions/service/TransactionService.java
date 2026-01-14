package com.example.transactions.service;

import com.example.transactions.dto.request.PutTransactionDto;
import com.example.transactions.dto.request.PostTransactionDto;
import com.example.transactions.dto.response.ModifyTransactionCurrencyDto;
import com.example.transactions.dto.response.TransactionDto;
import com.example.transactions.entity.Transaction;
import com.example.transactions.enums.Currency;
import com.example.transactions.enums.TransactionStatus;
import com.example.transactions.enums.TransactionType;
import com.example.transactions.mapper.TransactionMapper;
import com.example.transactions.repository.ITransactionRepository;
import com.example.transactions.service.ITransactionService;
import com.example.transactions.utils.TransactionHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.math.BigDecimal;
import java.util.Optional;

@Service
public class TransactionService implements ITransactionService {
    @Autowired
    private ITransactionRepository transactionRepository;

    @Override
    public TransactionDto postTransaction(PostTransactionDto transactionDto) {
        Transaction transaction = TransactionMapper.ToEntity(transactionDto);
        transactionRepository.save(transaction);
        return TransactionMapper.ToDto(transaction);
    }

    @Override
    public TransactionDto fetchTransactionById(String transactionId) {
        Optional<Transaction> transaction = transactionRepository.findTransactionByTransactionId(transactionId);

        if(transaction.isPresent()) {
            return TransactionMapper.ToDto(transaction.get());
        } else {
            throw new RuntimeException("Transaction with ID " + transactionId + " not found in method fetchTransactionById");
        }
    }

    @Override
    public boolean putTransaction(PutTransactionDto transactionDto, String transactionId) {
        Optional<Transaction> transactionToPatch = transactionRepository.findTransactionByTransactionId(transactionId);

        if (transactionToPatch.isPresent()) {
            Transaction existing = transactionToPatch.get();
            Transaction updated = TransactionMapper.ToEntity(transactionDto, existing.getId(), transactionId);
            transactionRepository.save(updated);
            return true;
        }
        throw new RuntimeException("Transaction with ID " + transactionId + " not found in method patchTransaction");
    }

    @Override
    public boolean cancelTransactionById(String transactionId) {
        Optional<Transaction> transactionToCancel = transactionRepository.findTransactionByTransactionId(transactionId);

        if(transactionToCancel.isPresent()) {
            transactionToCancel.get().setStatus(TransactionStatus.CANCELLED);
            transactionRepository.save(transactionToCancel.get());
            return true;
        }
        throw new RuntimeException("Transaction with ID " + transactionId + " not found in method cancelTransactionById");
    }

    @Override
    public boolean executePaymentByTransactionId(String transactionId) {
        Optional<Transaction> transactionToExecutePayment = transactionRepository.findTransactionByTransactionId(transactionId);

        if(transactionToExecutePayment.isPresent()) {
            transactionToExecutePayment.get().setStatus(TransactionStatus.COMPLETED);
            // further implementation to move money from one account to another
            transactionRepository.save(transactionToExecutePayment.get());
            return true;
        }
        throw new RuntimeException("Transaction with ID " + transactionId + " not found in method executePaymentByTransactionId");
    }

    @Override
    public boolean modifyTransactionType(String transactionId, TransactionType newTransactionType) {
        Optional<Transaction> transactionToExecutePayment = transactionRepository.findTransactionByTransactionId(transactionId);

        if(transactionToExecutePayment.isPresent()) {
            transactionToExecutePayment.get().setTransactionType(newTransactionType);
            transactionRepository.save(transactionToExecutePayment.get());
            return true;
        }
        throw new RuntimeException("Transaction with ID " + transactionId + " not found in method executePaymentByTransactionId");
    }

    @Override
    public ModifyTransactionCurrencyDto modifyTransactionCurrency(String transactionId, Currency newCurrency) {
        Optional<Transaction> transactionToModifyCurrency = transactionRepository.findTransactionByTransactionId(transactionId);

        if(transactionToModifyCurrency.isPresent()) {
            Currency previousCurrency = transactionToModifyCurrency.get().getCurrency();
            transactionToModifyCurrency.get().setCurrency(newCurrency);
            BigDecimal newAmount = (TransactionHelper.ConvertCurrency(
                    previousCurrency,
                    newCurrency,
                    transactionToModifyCurrency.get().getAmount()
            ));
            transactionToModifyCurrency.get().setAmount(newAmount);
            transactionRepository.save(transactionToModifyCurrency.get());

            return new ModifyTransactionCurrencyDto(
                    transactionId,
                    newCurrency,
                    newAmount
            );
        }
        throw new RuntimeException("Transaction with ID " + transactionId + " not found in method modifyTransactionCurrency");
    }

    @Override
    public BigDecimal calculateTransactionAmount(String transactionId) {
        Optional<Transaction> transaction = transactionRepository.findTransactionByTransactionId(transactionId);

        if(transaction.isPresent()) {
            return switch (transaction.get().getTransactionType()) {
                case TRANSFER -> transaction.get().getAmount().multiply(new BigDecimal("0.01"));
                case WITHDRAWAL -> new BigDecimal("2.50");
                case DEPOSIT -> BigDecimal.ZERO;
                case PAYMENT -> transaction.get().getAmount().multiply(new BigDecimal("0.015"));
                default -> BigDecimal.ZERO;
            };
        }
        throw new RuntimeException("Transaction with ID " + transactionId + " not found in method generateTransactionAmount");
    }

    @Override
    public String antiFraudCheck(String transactionId) {
        Optional<Transaction> transaction = transactionRepository.findTransactionByTransactionId(transactionId);

        if(transaction.isPresent() && transaction.get().getStatus() == TransactionStatus.COMPLETED) {
            return TransactionHelper.checkAmountAntiFraud(transaction.get()).toString();
        }
        throw new RuntimeException("Transaction with ID " + transactionId + " not found in method antiFraudCheck");
    }
}
