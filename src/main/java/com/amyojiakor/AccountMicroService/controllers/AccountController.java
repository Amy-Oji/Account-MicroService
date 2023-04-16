package com.amyojiakor.AccountMicroService.controllers;

import com.amyojiakor.AccountMicroService.models.payloads.AccountRequest;
import com.amyojiakor.AccountMicroService.services.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/v1/account")
public class AccountController {
    private final AccountService accountService;
    @PostMapping("create-account")
    public ResponseEntity<?> checkActivate(@RequestBody AccountRequest accountRequest){
        return ResponseEntity.ok(accountService.createAccount(accountRequest));
    }

}
