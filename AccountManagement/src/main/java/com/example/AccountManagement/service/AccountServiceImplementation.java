package com.example.AccountManagement.service;


import com.example.AccountManagement.dto.AccountBalanceDto;
import com.example.AccountManagement.dto.AccountCreateDto;
import com.example.AccountManagement.dto.AccountDto;
import com.example.AccountManagement.dto.AccountUpdateDto;
import com.example.AccountManagement.entity.Account;
import com.example.AccountManagement.enums.AccountStatus;
import com.example.AccountManagement.mapper.AccountMapper;
import com.example.AccountManagement.repository.IAccountRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AccountServiceImplementation implements IAccountService {

@Autowired
private IAccountRepository accountRepository;

    @Override
    @Transactional
    public AccountDto createAccount(AccountCreateDto accountCreateDto){
        Account account = AccountMapper.toEntity(accountCreateDto);
        account.setStatus(AccountStatus.PENDING);
        Account savedAccount = accountRepository.save(account);

        return AccountMapper.toDto(savedAccount);
    }

    @Override
    public AccountDto fetchAccount(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountNumber));
        return AccountMapper.toDto(account);
    }

    @Override
    @Transactional
    public AccountDto updateAccount(AccountUpdateDto accountUpdateDto) {
        Account account = accountRepository.findByAccountNumber(accountUpdateDto.getAccountNumber())
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountUpdateDto.getAccountNumber()));

        AccountMapper.updateEntityFromDto(account, accountUpdateDto);

        Account updatedAccount = accountRepository.save(account);

        return AccountMapper.toDto(updatedAccount);
    }

    @Override
    @Transactional
    public boolean closeAccount(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountNumber));

        account.setStatus(AccountStatus.CLOSED);
        accountRepository.save(account);

        return true;
    }

    @Override
    public AccountBalanceDto checkBalance(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountNumber));


        return AccountMapper.toBalanceDto(account);
    }

    @Override
    @Transactional
    public String blockAccount(String accountNumber, String reason) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountNumber));

        account.setStatus(AccountStatus.BLOCKED);
        account.setBlockedAt(LocalDateTime.now());
        account.setBlockedReason(reason);
        accountRepository.save(account);

        return "Account blocked successfully. Reason: " + reason;
    }

    @Override
    @Transactional
    public String unblockAccount(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountNumber));

        if (account.getStatus() != AccountStatus.BLOCKED) {
            throw new RuntimeException("Account is not blocked");
        }

        account.setStatus(AccountStatus.ACTIVE);
        account.setBlockedAt(null);
        account.setBlockedReason(null);
        accountRepository.save(account);

        return "Account unblocked successfully";
    }

    @Override
    @Transactional
    public String verifyAccount(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountNumber));

        account.setIsVerified(true);
        account.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(account);



        return "Account verified successfully";
    }

    @Override
    public String generateAccountHistory(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountNumber));



        StringBuilder history = new StringBuilder();
        history.append("Account History for: ").append(accountNumber).append("\n");
        history.append("Customer: ").append(account.getCustomerName()).append("\n");
        history.append("Account Type: ").append(account.getAccountType()).append("\n");
        history.append("Status: ").append(account.getStatus()).append("\n");
        history.append("Current Balance: ").append(account.getBalance()).append(" ").append(account.getCurrency()).append("\n");
        history.append("Created At: ").append(account.getCreatedAt()).append("\n");
        history.append("Last Updated: ").append(account.getUpdatedAt()).append("\n");
        history.append("Verified: ").append(account.getIsVerified() ? "Yes" : "No").append("\n");

        if (account.getBlockedAt() != null) {
            history.append("Blocked At: ").append(account.getBlockedAt()).append("\n");
            history.append("Block Reason: ").append(account.getBlockedReason()).append("\n");
        }

        return history.toString();
    }

    @Override
    public List<AccountDto> getAccountsByStatus(AccountStatus status) {
        List<Account> accounts = accountRepository.findByStatus(status);
        return accounts.stream()
                .map(AccountMapper::toDto)
                .collect(Collectors.toList());
    }


    @Override
    public List<AccountDto> getAllAccountsSorted(String sortBy, String sortOrder) {
        List<Account> accounts = accountRepository.findAll();


        switch (sortBy.toLowerCase()) {
            case "balance":
                accounts.sort(Comparator.comparing(Account::getBalance));
                break;
            case "customername":
                accounts.sort(Comparator.comparing(Account::getCustomerName));
                break;
            default:
                accounts.sort(Comparator.comparing(Account::getId));
        }


        if ("desc".equalsIgnoreCase(sortOrder)) {
            Collections.reverse(accounts);
        }

        return accounts.stream()
                .map(AccountMapper::toDto)
                .collect(Collectors.toList());
    }


}
