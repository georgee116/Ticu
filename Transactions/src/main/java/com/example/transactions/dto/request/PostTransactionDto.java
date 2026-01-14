package com.example.transactions.dto.request;

import com.example.transactions.enums.Currency;
import com.example.transactions.enums.TransactionStatus;
import com.example.transactions.enums.TransactionType;

import java.math.BigDecimal;

public class PostTransactionDto {
    private String fromAccountId;
    private String toAccountId;
    private String fromAccountNumber;
    private String toAccountNumber;
    private TransactionType transactionType;
    private BigDecimal amount;
    private Currency currency;
    private String description;

    public PostTransactionDto(
            String toAccountNumber,
            String fromAccountId,
            String toAccountId,
            String fromAccountNumber,
            TransactionType transactionType,
            BigDecimal amount,
            Currency currency,
            String description) {
        this.toAccountNumber = toAccountNumber;
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.fromAccountNumber = fromAccountNumber;
        this.transactionType = transactionType;
        this.amount = amount;
        this.currency = currency;
        this.description = description;
    }


    public String getToAccountId() {
        return toAccountId;
    }

    public void setToAccountId(String toAccountId) {
        this.toAccountId = toAccountId;
    }

    public String getFromAccountId() {
        return fromAccountId;
    }

    public void setFromAccountId(String fromAccountId) {
        this.fromAccountId = fromAccountId;
    }

    public String getFromAccountNumber() {
        return fromAccountNumber;
    }

    public void setFromAccountNumber(String fromAccountNumber) {
        this.fromAccountNumber = fromAccountNumber;
    }

    public String getToAccountNumber() {
        return toAccountNumber;
    }

    public void setToAccountNumber(String toAccountNumber) {
        this.toAccountNumber = toAccountNumber;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
