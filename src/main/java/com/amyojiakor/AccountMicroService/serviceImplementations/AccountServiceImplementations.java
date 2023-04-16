package com.amyojiakor.AccountMicroService.serviceImplementations;

import com.amyojiakor.AccountMicroService.models.entities.Account;
import com.amyojiakor.AccountMicroService.models.payloads.AccountRequest;
import com.amyojiakor.AccountMicroService.models.payloads.AccountResponse;
import com.amyojiakor.AccountMicroService.repositories.AccountRepository;
import com.amyojiakor.AccountMicroService.services.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AccountServiceImplementations implements AccountService {
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

        return new AccountResponse(
                accountNum,
                accountRequest.accountName(),
                accountRequest.accountType(),
                accountRequest.currencyCode(),
                account.getAccountBalance()
        );
    }

    @Override
    public AccountResponse updateAccount(AccountRequest accountRequest) {
        return null;
    }

    @Override
    public AccountResponse getAccountDetails(String accountNumber) {
        return null;
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
}
