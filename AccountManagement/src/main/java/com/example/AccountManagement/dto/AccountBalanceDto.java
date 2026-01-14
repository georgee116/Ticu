package com.example.AccountManagement.dto;

import com.example.AccountManagement.enums.Currency;

import java.math.BigDecimal;
//validation dto
public class AccountBalanceDto {
    private String accountNumber;
    private BigDecimal balance;
    private Currency currency;
    private BigDecimal dailyLimit;
    private BigDecimal monthlyLimit;


    public AccountBalanceDto() {
    }

    public AccountBalanceDto(String accountNumber, BigDecimal balance,
                             Currency currency, BigDecimal dailyLimit, BigDecimal monthlyLimit) {
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.currency = currency;
        this.dailyLimit = dailyLimit;
        this.monthlyLimit = monthlyLimit;
    }


    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }


    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public BigDecimal getDailyLimit() {
        return dailyLimit;
    }

    public void setDailyLimit(BigDecimal dailyLimit) {
        this.dailyLimit = dailyLimit;
    }

    public BigDecimal getMonthlyLimit() {
        return monthlyLimit;
    }

    public void setMonthlyLimit(BigDecimal monthlyLimit) {
        this.monthlyLimit = monthlyLimit;
    }
}
