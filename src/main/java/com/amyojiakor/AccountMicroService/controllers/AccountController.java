package com.amyojiakor.AccountMicroService.controllers;

import com.amyojiakor.AccountMicroService.models.payloads.AccountRequest;
import com.amyojiakor.AccountMicroService.models.payloads.UpdateAccountRequest;
import com.amyojiakor.AccountMicroService.services.AccountService;
import com.amyojiakor.AccountMicroService.services.serviceImplementations.AccountServiceImplementations;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Base64;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/v1/accounts")
public class AccountController {
    @Autowired
    private final AccountServiceImplementations accountService;

    @PostMapping("create-account")
    public ResponseEntity<?> createAccount(@RequestBody AccountRequest accountRequest, @RequestHeader("Authorization") String token){
        String encodedToken = Base64.getUrlEncoder().withoutPadding().encodeToString(token.getBytes());
        return ResponseEntity.ok(accountService.createAccount(accountRequest, encodedToken));
    }

    @PostMapping("update-account")
    public ResponseEntity<?> updateAccount(@RequestBody UpdateAccountRequest accountRequest){
        return ResponseEntity.ok(accountService.updateAccount(accountRequest));
    }

    @GetMapping("/get-account/{accountNumber}")
    public ResponseEntity<?> getAccountDetails(@PathVariable String accountNumber,  @RequestHeader("Authorization") String token) {
        System.out.println("Received token: " + token);
        return ResponseEntity.ok(accountService.getAccountDetails(accountNumber));
    }

}
