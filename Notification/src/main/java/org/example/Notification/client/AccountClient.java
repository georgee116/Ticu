package org.example.Notification.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "accountmanagement", path = "/api/accounts")
public interface AccountClient {

    @GetMapping("/fetch_general_data")
    ResponseEntity<Object> getAccount(@RequestParam("accountNumber") String accountNumber);

    @GetMapping("/check_balance")
    ResponseEntity<Object> checkBalance(@RequestParam("accountNumber") String accountNumber);
}