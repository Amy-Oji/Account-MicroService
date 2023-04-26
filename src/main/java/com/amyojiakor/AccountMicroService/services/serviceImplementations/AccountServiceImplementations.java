package com.amyojiakor.AccountMicroService.services.serviceImplementations;

import com.amyojiakor.AccountMicroService.models.entities.Account;
import com.amyojiakor.AccountMicroService.models.payloads.AccountRequest;
import com.amyojiakor.AccountMicroService.models.payloads.AccountResponse;
import com.amyojiakor.AccountMicroService.models.payloads.UpdateAccountRequest;
import com.amyojiakor.AccountMicroService.repositories.AccountRepository;
import com.amyojiakor.AccountMicroService.services.AccountService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Random;

@Service
public class AccountServiceImplementations implements AccountService {

    private final AccountRepository accountRepository;
    private final String accountCreationTopic;
    private final KafkaTemplate<String, AccountResponse> kafkaTemplate;

    @Autowired
    public AccountServiceImplementations(AccountRepository accountRepository, @Value("${kafka.topic.account-creation}") String accountCreationTopic, KafkaTemplate<String, AccountResponse> kafkaTemplate) {
        this.accountRepository = accountRepository;
        this.accountCreationTopic = accountCreationTopic;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public AccountResponse createAccount(AccountRequest accountRequest) {

        Account account = new Account();
        BeanUtils.copyProperties(accountRequest, account);
        account.setAccountBalance(BigDecimal.valueOf(0));

        String accountNum = generateAccountNumber();
        while(!isUnique(accountNum)) accountNum = generateAccountNumber();
        account.setAccountNumber(accountNum);

        accountRepository.save(account);

        AccountResponse accountResponse = mapToAccountResponse(account);

        kafkaTemplate.send(accountCreationTopic, accountResponse);

        return accountResponse;
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
                account.getEmail(),
                account.getAccountNumber(),
                account.getAccountName(),
                account.getAccountType(),
                account.getCurrencyCode(),
                account.getAccountBalance()
        );
    }
}
