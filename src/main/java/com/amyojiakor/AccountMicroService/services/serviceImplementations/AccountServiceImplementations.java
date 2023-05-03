package com.amyojiakor.AccountMicroService.services.serviceImplementations;

import com.amyojiakor.AccountMicroService.config.ApiConfig;
import com.amyojiakor.AccountMicroService.models.entities.Account;
import com.amyojiakor.AccountMicroService.models.enums.TransactionType;
import com.amyojiakor.AccountMicroService.models.payloads.*;
import com.amyojiakor.AccountMicroService.repositories.AccountRepository;
import com.amyojiakor.AccountMicroService.services.AccountService;
import jakarta.transaction.Transactional;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Base64;
import java.util.Optional;
import java.util.Random;

@Service
public class AccountServiceImplementations implements AccountService {

    private final AccountRepository accountRepository;
    private final String accountCreationTopic;

    private final KafkaTemplate<String, AccountResponse> accountKafkaTemplate;
    private final ApiConfig apiConfig;
    private final RestTemplate restTemplate;
    private final String balanceUpdateTopic;

    private final KafkaTemplate<String, TransactionMessageResponse> transactionKafkaTemplate;

    @Autowired
    public AccountServiceImplementations(AccountRepository accountRepository, @Value("${kafka.topic.account.creation}") String accountCreationTopic, KafkaTemplate<String, AccountResponse> accountKafkaTemplate, ApiConfig apiConfig, RestTemplate restTemplate, @Value("${kafka.topic.account.balance-update}") String balanceUpdateTopic, KafkaTemplate<String, TransactionMessageResponse> transactionKafkaTemplate) {
        this.accountRepository = accountRepository;
        this.accountCreationTopic = accountCreationTopic;
        this.accountKafkaTemplate = accountKafkaTemplate;
        this.apiConfig = apiConfig;
        this.restTemplate = restTemplate;
        this.balanceUpdateTopic = balanceUpdateTopic;
        this.transactionKafkaTemplate = transactionKafkaTemplate;
    }

    @Transactional
    @Override
    public AccountResponse createAccount(AccountRequest accountRequest, String token) {

        UserDetailsResponse user = getUser(token);
        assert user != null;

        Account account = new Account();
        BeanUtils.copyProperties(accountRequest, account);
        account.setAccountBalance(BigDecimal.valueOf(0));
        account.setEmail(user.email());
        account.setAccountName(user.firstName() + " " + user.lastName());

        String accountNum = generateAccountNumber();
        while(!isUnique(accountNum)) accountNum = generateAccountNumber();

        account.setAccountNumber(accountNum);

        accountRepository.save(account);

        AccountResponse accountResponse = mapToAccountResponse(account);

        accountKafkaTemplate.send(accountCreationTopic, accountResponse);

        return accountResponse;
    }

    @Override
    public AccountResponse updateAccount( String accountNum, UpdateAccountRequest accountRequest) {
        var account = accountRepository.findByAccountNumber(accountNum).orElseThrow();
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

    private UserDetailsResponse getUser(String token){
        byte[] decodedToken = Base64.getUrlDecoder().decode(token);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(new String(decodedToken));
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<UserDetailsResponse> responseEntity =
                restTemplate.exchange(
                        apiConfig.getUserServiceBaseUrl()+"get-user-details",
                        HttpMethod.GET,
                        entity,
                        UserDetailsResponse.class);

        return responseEntity.getBody();
    }

    @Transactional
    @KafkaListener(topics = "${kafka.topic.account.transact}", groupId = "${spring.kafka.consumer.group-id}", containerFactory = "transactionListenerContainerFactory")
    public void consume(TransactionMessage transactionMessage) throws Exception {
        TransactionMessageResponse transactionMessageResponse = processTransaction(transactionMessage);
        transactionKafkaTemplate.send(balanceUpdateTopic, transactionMessageResponse);
        System.out.println(transactionMessageResponse);
    }

    private TransactionMessageResponse processTransaction(TransactionMessage transactionMessage) throws Exception {
        TransactionMessageResponse transactionMessageResponse = new TransactionMessageResponse();
        var account = accountRepository.findByAccountNumber(transactionMessage.accountNum()).orElseThrow();
        var balance = account.getAccountBalance();
        if (transactionMessage.transactionType() == TransactionType.CREDIT) {
            account.setAccountBalance(balance.add(transactionMessage.amount()));
        } else {
            if (transactionMessage.amount().compareTo(balance) > 0) {
                transactionMessageResponse.setTransactionStatus(TransactionStatus.FAILED);
                transactionMessageResponse.setErrorMessage("Insufficient Funds");
                throw new Exception("Insufficient Funds");
            }
            account.setAccountBalance(balance.subtract(transactionMessage.amount()));
        }
        transactionMessageResponse.setTransactionStatus(TransactionStatus.COMPLETED);
        transactionMessageResponse.setNewAccountBalance(account.getAccountBalance());
        transactionMessageResponse.setAccountNum(account.getAccountNumber());
        transactionMessageResponse.setErrorMessage(null);
        accountRepository.save(account);
        return transactionMessageResponse;
    }
}
