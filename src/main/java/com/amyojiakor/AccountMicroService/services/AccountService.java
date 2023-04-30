package com.amyojiakor.AccountMicroService.services;

import com.amyojiakor.AccountMicroService.models.payloads.AccountRequest;
import com.amyojiakor.AccountMicroService.models.payloads.AccountResponse;
import com.amyojiakor.AccountMicroService.models.payloads.UpdateAccountRequest;

public interface AccountService {
    AccountResponse createAccount(AccountRequest accountRequest, String token);
    AccountResponse updateAccount(String accountNum, UpdateAccountRequest updateAccountRequest);
    AccountResponse getAccountDetails(String accountNumber);

}
