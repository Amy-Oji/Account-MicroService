package com.amyojiakor.AccountMicroService.controllers;

import com.amyojiakor.AccountMicroService.models.payloads.AccountRequest;
import com.amyojiakor.AccountMicroService.models.payloads.UpdateAccountRequest;
import com.amyojiakor.AccountMicroService.services.AccountService;
import com.amyojiakor.AccountMicroService.services.serviceImplementations.AccountServiceImplementations;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/v1/accounts")
public class AccountController {
    @Autowired
    private final AccountServiceImplementations accountService;

    @PostMapping("create-account")
    public ResponseEntity<?> createAccount(@RequestBody AccountRequest accountRequest){
        return ResponseEntity.ok(accountService.createAccount(accountRequest));
    }

    @PostMapping("update-account")
    public ResponseEntity<?> updateAccount(@RequestBody UpdateAccountRequest accountRequest){
        return ResponseEntity.ok(accountService.updateAccount(accountRequest));
    }

    @GetMapping("/get-account/{accountNumber}")
    public ResponseEntity<?> getAccountDetails(@PathVariable String accountNumber){
        return ResponseEntity.ok(accountService.getAccountDetails(accountNumber));
    }

}
