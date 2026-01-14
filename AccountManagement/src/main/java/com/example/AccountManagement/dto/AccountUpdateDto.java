package com.example.AccountManagement.dto;

import java.math.BigDecimal;
//update data
public class AccountUpdateDto {

    private String accountNumber;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private BigDecimal dailyLimit;
    private BigDecimal monthlyLimit;


    public AccountUpdateDto() {
    }

    public AccountUpdateDto(String accountNumber, String customerName, String customerEmail,
                            String customerPhone, BigDecimal dailyLimit, BigDecimal monthlyLimit) {
        this.accountNumber = accountNumber;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.customerPhone = customerPhone;
        this.dailyLimit = dailyLimit;
        this.monthlyLimit = monthlyLimit;
    }


    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
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
