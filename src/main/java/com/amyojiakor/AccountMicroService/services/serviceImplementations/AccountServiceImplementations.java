package com.amyojiakor.AccountMicroService.services.serviceImplementations;

import com.amyojiakor.AccountMicroService.config.ApiConfig;
import com.amyojiakor.AccountMicroService.models.entities.Account;
import com.amyojiakor.AccountMicroService.models.enums.TransactionStatus;
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
    public AccountServiceImplementations(AccountRepository accountRepository,
                                         @Value("${kafka.topic.account.creation}") String accountCreationTopic,
                                         KafkaTemplate<String, AccountResponse> accountKafkaTemplate,
                                         ApiConfig apiConfig,
                                         RestTemplate restTemplate,
                                         @Value("${kafka.topic.account.balance-update}") String balanceUpdateTopic,
                                         KafkaTemplate<String, TransactionMessageResponse> transactionKafkaTemplate ) {
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
    public AccountDetailsApiResponse getAccountDetails(String accountNumber) throws Exception {
        var account = accountRepository.findByAccountNumber(accountNumber).orElseThrow(()-> new Exception("Account Not Found"));
        return new AccountDetailsApiResponse(account.getAccountNumber(), account.getAccountName(), account.getCurrencyCode());
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
        TransactionMessageResponse response;
        if(transactionMessage.transactionType().equals(TransactionType.INTERNAL_TRANSFER)){
            response = processInternalTransfer(transactionMessage);
            transactionKafkaTemplate.send(balanceUpdateTopic, response);
            System.out.println(response);
            System.out.println(response + " ============== in consume 1111");
        } else {
        response = processTransaction(transactionMessage);
        transactionKafkaTemplate.send(balanceUpdateTopic, response);
        System.out.println(response + " ============== in consume2");
        }
    }

    private TransactionMessageResponse processTransaction(TransactionMessage transactionMessage) throws Exception {
        TransactionMessageResponse transactionMessageResponse = new TransactionMessageResponse();
        var account = accountRepository.findByAccountNumber(transactionMessage.sourceAccountNumber()).orElseThrow();
        var balance = account.getAccountBalance();
        if (transactionMessage.transactionType() == TransactionType.CREDIT) {
            account.setAccountBalance(balance.add(transactionMessage.amount()));
        } else {
            if (transactionMessage.amount().compareTo(balance) > 0) {
                transactionMessageResponse.setStatus(TransactionStatus.FAILED);
                transactionMessageResponse.setErrorMessage("Insufficient Funds");
                throw new Exception("Insufficient Funds");
            }
            account.setAccountBalance(balance.subtract(transactionMessage.amount()));
        }
        transactionMessageResponse.setStatus(TransactionStatus.COMPLETED);
        transactionMessageResponse.setNewAccountBalance(account.getAccountBalance());
        transactionMessageResponse.setSourceAccountNumber(account.getAccountNumber());
        transactionMessageResponse.setErrorMessage(null);
        accountRepository.save(account);
        return transactionMessageResponse;
    }

    private TransactionMessageResponse processInternalTransfer(TransactionMessage transactionMessage) throws Exception {

        TransactionMessageResponse response = new TransactionMessageResponse();
        BeanUtils.copyProperties(transactionMessage, response);

        var sourceAccount = accountRepository.findByAccountNumber(transactionMessage.sourceAccountNumber()).orElseThrow();
        var recipientAccount = accountRepository.findByAccountNumber(transactionMessage.recipientAccountNumber()).orElseThrow();

        if (transactionMessage.amount().compareTo(sourceAccount.getAccountBalance()) > 0) {
            response.setStatus(TransactionStatus.FAILED);
            throw new Exception("Insufficient Funds");
        }
        sourceAccount.setAccountBalance(sourceAccount.getAccountBalance().subtract(transactionMessage.amount()));
        recipientAccount.setAccountBalance(recipientAccount.getAccountBalance().add(transactionMessage.amount()));
        response.setStatus(TransactionStatus.COMPLETED);
        response.setNewAccountBalance(sourceAccount.getAccountBalance());
        accountRepository.save(sourceAccount);
        accountRepository.save(recipientAccount);
        System.out.println(response + "   ================== in processInternalTransfer");
        return response;
    }
}
