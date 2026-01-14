package org.example.Notification.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "transactions", path = "/api/transactions")
public interface TransactionClient {

    @PostMapping("/post")
    ResponseEntity<Object> createTransaction(@RequestBody Object transactionDto);

    @GetMapping("/get/{transactionId}")
    ResponseEntity<Object> getTransaction(@PathVariable("transactionId") String transactionId);

    @PutMapping("/put/{transactionId}")
    ResponseEntity<String> putTransaction(
            @PathVariable("transactionId") String transactionId,
            @RequestBody Object putTransactionDto);

    @DeleteMapping("/close/{transactionId}")
    ResponseEntity<String> cancelTransaction(@PathVariable("transactionId") String transactionId);

    @GetMapping("/calculate-fees/{transactionId}")
    ResponseEntity<String> calculateFees(@PathVariable("transactionId") String transactionId);

    @GetMapping("/anti-fraud-check/{transactionId}")
    ResponseEntity<String> antiFraudCheck(@PathVariable("transactionId") String transactionId);
}