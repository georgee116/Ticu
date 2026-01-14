package com.example.transactions.controller;

import com.example.transactions.dto.request.PutTransactionDto;
import com.example.transactions.dto.request.PostTransactionDto;
import com.example.transactions.dto.response.ModifyTransactionCurrencyDto;
import com.example.transactions.dto.response.TransactionDto;
import com.example.transactions.enums.Currency;
import com.example.transactions.enums.TransactionType;
import com.example.transactions.service.ITransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping(path = "/api/transactions")
@io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "Success",
        content = @io.swagger.v3.oas.annotations.media.Content
)
public class TransactionController {
    @Autowired
    private ITransactionService transactionService;

    // CRUD operations
    @PostMapping("/post")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<TransactionDto> postTransaction(@RequestBody PostTransactionDto postTransactionDto) {
        TransactionDto createdTransaction = transactionService.postTransaction(postTransactionDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdTransaction);
    }

    @PutMapping("/put/{transactionId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<String> putTransaction(
            @PathVariable String transactionId,
            @RequestBody PutTransactionDto putTransactionDto) {
        boolean success = transactionService.putTransaction(putTransactionDto, transactionId);
        if(success) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body("Transaction put successfully");
        } else {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Transaction not found");
        }
    }

    @GetMapping("/get/{transactionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TransactionDto> getTransaction(@PathVariable("transactionId") String transactionId) {
        TransactionDto transaction = transactionService.fetchTransactionById(transactionId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(transaction);
    }

    @DeleteMapping("/close/{transactionId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<String> cancelTransaction(@PathVariable("transactionId") String transactionId) {
        boolean success = transactionService.cancelTransactionById(transactionId);
        if(success) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body("Transaction canceled successfully");
        } else {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Transaction not found");
        }
    }

    @PatchMapping("/modify-transaction-type/{transactionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> modifyTransactionType(
            @PathVariable("transactionId") String transactionId,
            @RequestParam("newType") TransactionType newType) {
        boolean success = transactionService.modifyTransactionType(transactionId, newType);
        if(success) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body("Transaction type modified successfully");
        } else {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Transaction not found");
        }
    }

    // extra operations
    @PatchMapping("/modify-currency/{transactionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ModifyTransactionCurrencyDto> modifyTransactionCurrency(
            @PathVariable String transactionId,
            @RequestParam Currency currency) {
        ModifyTransactionCurrencyDto result = transactionService.modifyTransactionCurrency(transactionId, currency);

        if(result != null) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(result);
        } else {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(null);
        }
    }

    @PatchMapping("/complete-payment/{transactionId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<String> completePayment(@PathVariable("transactionId") String transactionId) {
        boolean success = transactionService.executePaymentByTransactionId(transactionId);
        if(success) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body("Transaction completed successfully");
        } else {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Transaction not found");
        }
    }

    @GetMapping("/calculate-fees/{transactionId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<String> calculateFees(@PathVariable("transactionId") String transactionId) {
        BigDecimal fees = transactionService.calculateTransactionAmount(transactionId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body("Calculated fees: " + fees.toString());
    }

    @GetMapping("/anti-fraud-check/{transactionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> antiFraudCheck(@PathVariable("transactionId") String transactionId) {
        String checkResult = transactionService.antiFraudCheck(transactionId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body("Anti fraud score:" + checkResult);
    }
}
