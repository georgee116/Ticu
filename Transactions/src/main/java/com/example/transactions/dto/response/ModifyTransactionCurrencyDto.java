package com.example.transactions.dto.response;

import com.example.transactions.enums.Currency;

import java.math.BigDecimal;

public class ModifyTransactionCurrencyDto {
    private String transactionId;
    private Currency currency;
    private BigDecimal amount;

    public ModifyTransactionCurrencyDto(String transactionId, Currency currency, BigDecimal amount) {
        this.transactionId = transactionId;
        this.currency = currency;
        this.amount = amount;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
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
}
