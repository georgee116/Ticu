package com.example.AccountManagement.mapper;

import com.example.AccountManagement.dto.AccountBalanceDto;
import com.example.AccountManagement.dto.AccountCreateDto;
import com.example.AccountManagement.dto.AccountDto;
import com.example.AccountManagement.dto.AccountUpdateDto;
import com.example.AccountManagement.entity.Account;

import java.util.UUID;

public class AccountMapper {

    public static Account toEntity(AccountCreateDto dto) {
        Account account = new Account(
                generateAccountNumber(),
                dto.getCustomerId(),
                dto.getCustomerName(),
                dto.getCustomerEmail(),
                dto.getCustomerPhone(),
                dto.getAccountType(),
                dto.getCurrency(),
                dto.getInitialDeposit(),
                null,
                null
        );

        return account;
    }

    public static AccountDto toDto(Account account) {
        return new AccountDto(
                account.getId(),
                account.getAccountNumber(),
                account.getCustomerId(),
                account.getCustomerName(),
                account.getCustomerEmail(),
                account.getCustomerPhone(),
                account.getAccountType(),
                account.getBalance(),
                account.getCurrency(),
                account.getStatus(),
                account.getIsVerified(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }

    public static AccountBalanceDto toBalanceDto(Account account) {
        return new AccountBalanceDto(
                account.getAccountNumber(),
                account.getBalance(),
                account.getCurrency(),
                account.getDailyLimit(),
                account.getMonthlyLimit()
        );
    }

    private static String generateAccountNumber() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String random = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return "RO" + timestamp.substring(timestamp.length() - 10) + random;
    }

    public static void updateEntityFromDto(Account account, AccountUpdateDto dto) {
        if (dto.getCustomerName() != null) {
            account.setCustomerName(dto.getCustomerName());
        }
        if (dto.getCustomerEmail() != null) {
            account.setCustomerEmail(dto.getCustomerEmail());
        }
        if (dto.getCustomerPhone() != null) {
            account.setCustomerPhone(dto.getCustomerPhone());
        }
        if (dto.getDailyLimit() != null) {
            account.setDailyLimit(dto.getDailyLimit());
        }
        if (dto.getMonthlyLimit() != null) {
            account.setMonthlyLimit(dto.getMonthlyLimit());
        }
    }
}
