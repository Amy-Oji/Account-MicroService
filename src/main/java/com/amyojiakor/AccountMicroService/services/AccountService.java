package com.amyojiakor.AccountMicroService.services;

import com.amyojiakor.AccountMicroService.models.payloads.AccountRequest;
import com.amyojiakor.AccountMicroService.models.payloads.AccountResponse;

public interface AccountService {
    AccountResponse createAccount(AccountRequest accountRequest);
    AccountResponse updateAccount(AccountRequest accountRequest);
    AccountResponse getAccountDetails(String accountNumber);

}
