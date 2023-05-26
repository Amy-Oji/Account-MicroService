package com.amyojiakor.AccountMicroService.models.payloads;

import com.amyojiakor.AccountMicroService.models.enums.CurrencyCode;

public record AccountDetailsApiResponse (String accountNumber, String accountName, CurrencyCode currencyCode){
}
