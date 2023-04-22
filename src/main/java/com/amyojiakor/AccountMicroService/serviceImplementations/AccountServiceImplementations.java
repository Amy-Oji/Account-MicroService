package com.amyojiakor.AccountMicroService.serviceImplementations;

import com.amyojiakor.AccountMicroService.models.entities.Account;
import com.amyojiakor.AccountMicroService.models.payloads.AccountRequest;
import com.amyojiakor.AccountMicroService.models.payloads.AccountResponse;
import com.amyojiakor.AccountMicroService.models.payloads.UpdateAccountRequest;
import com.amyojiakor.AccountMicroService.repositories.AccountRepository;
import com.amyojiakor.AccountMicroService.services.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AccountServiceImplementations implements AccountService {

   @Autowired
    private final AccountRepository accountRepository;

    @Override
    public AccountResponse createAccount(AccountRequest accountRequest) {

        Account account = new Account();
        BeanUtils.copyProperties(accountRequest, account);
        account.setAccountBalance(BigDecimal.valueOf(0));

        String accountNum = generateAccountNumber();
        while(!isUnique(accountNum)) accountNum = generateAccountNumber();
        account.setAccountNumber(accountNum);

        accountRepository.save(account);

        return mapToAccountResponse(account);
    }

    @Override
    public AccountResponse updateAccount(UpdateAccountRequest accountRequest) {
        var account = accountRepository.findByAccountNumber(accountRequest.accountNumber()).orElseThrow();
        BeanUtils.copyProperties(accountRequest, account);
        accountRepository.save(account);
        return mapToAccountResponse(account);
    }

    @Override
    public AccountResponse getAccountDetails(String accountNumber) {
        var account = accountRepository.findByAccountNumber(accountNumber).orElseThrow();
        return mapToAccountResponse(account);
    }

    private static String generateAccountNumber() {
        Random rand = new Random();
        int num = rand.nextInt(900000000) + 1000000000;
        return Integer.toString(num);
    }

    private Boolean isUnique(String accountNumber) {
        Optional<Account> bankAccount = accountRepository.findByAccountNumber(accountNumber);
        return bankAccount.isEmpty();
    }

    private AccountResponse mapToAccountResponse(Account account){
        return new AccountResponse(
                account.getAccountNumber(),
                account.getAccountName(),
                account.getAccountType(),
                account.getCurrencyCode(),
                account.getAccountBalance()
        );
    }
}
