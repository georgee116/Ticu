package com.example.transactions.service;

import com.example.transactions.dto.request.PostTransactionDto;
import com.example.transactions.dto.request.PutTransactionDto;
import com.example.transactions.dto.response.ModifyTransactionCurrencyDto;
import com.example.transactions.dto.response.TransactionDto;
import com.example.transactions.enums.Currency;
import com.example.transactions.enums.TransactionType;

import java.math.BigDecimal;

public interface ITransactionService {
    TransactionDto postTransaction(PostTransactionDto transactionDto);
    TransactionDto fetchTransactionById(String transactionId);
    boolean putTransaction(PutTransactionDto transactionDto, String transactionId);
    boolean cancelTransactionById(String transactionId);
    boolean executePaymentByTransactionId(String transactionId);
    boolean modifyTransactionType(String transactionId, TransactionType newTransactionType);
    ModifyTransactionCurrencyDto modifyTransactionCurrency(String transactionId, Currency newCurrency);
    BigDecimal calculateTransactionAmount(String transactionId);
    String antiFraudCheck(String transactionId);
}
