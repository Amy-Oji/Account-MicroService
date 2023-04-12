package com.amyojiakor.AccountMicroService.controllers;

import com.amyojiakor.AccountMicroService.models.AccountDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("account")
public class AccountController {

    @PostMapping("test")
    public ResponseEntity<?> checkActivate(@RequestBody AccountDto accountDto){

        System.out.println(accountDto.getName());

        return ResponseEntity.ok(new AccountDto(accountDto.getName()));
    }


}
