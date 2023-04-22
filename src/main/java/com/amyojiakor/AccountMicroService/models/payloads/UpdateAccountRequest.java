package com.amyojiakor.AccountMicroService.models.payloads;

import com.amyojiakor.AccountMicroService.models.enums.AccountType;
import com.amyojiakor.AccountMicroService.models.enums.CurrencyCode;

public record UpdateAccountRequest(String accountName, AccountType accountType, CurrencyCode currencyCode, String accountNumber) {
}
