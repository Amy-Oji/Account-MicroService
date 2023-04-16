package com.amyojiakor.AccountMicroService.models.payloads;

import com.amyojiakor.AccountMicroService.models.enums.AccountType;
import com.amyojiakor.AccountMicroService.models.enums.CurrencyCode;

public record AccountRequest (String accountName, AccountType accountType, CurrencyCode currencyCode, String string){
}
