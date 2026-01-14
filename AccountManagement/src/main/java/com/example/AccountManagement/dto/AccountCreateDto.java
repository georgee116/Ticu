package com.example.AccountManagement.dto;

import com.example.AccountManagement.enums.AccountType;
import com.example.AccountManagement.enums.Currency;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
//create acc
public class AccountCreateDto {

    @Schema(description = "Customer ID", example = "12345")
    private Long customerId;
    @Schema(description = "Customer name", example = "Ion Popescu")
    private String customerName;
    @Schema(description = "Customer email", example = "ion.popescu@email.com")
    private String customerEmail;
    @Schema(description = "Customer phone", example = "0722123456")
    private String customerPhone;
    @Schema(description = "Account type", example = "SAVINGS")
    private AccountType accountType;
    @Schema(description = "Currency", example = "RON")
    private Currency currency;
    @Schema(description = "Initial deposit", example = "1000.00")
    private BigDecimal initialDeposit;


    public AccountCreateDto() {

    }

    public AccountCreateDto(Long customerId, String customerName, String customerEmail, String customerPhone, AccountType accountType, Currency currency, BigDecimal initialDeposit) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.customerPhone = customerPhone;
        this.accountType = accountType;
        this.currency = currency;
        this.initialDeposit = initialDeposit;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
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

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public BigDecimal getInitialDeposit() {
        return initialDeposit;
    }

    public void setInitialDeposit(BigDecimal initialDeposit) {
        this.initialDeposit = initialDeposit;
    }
}
