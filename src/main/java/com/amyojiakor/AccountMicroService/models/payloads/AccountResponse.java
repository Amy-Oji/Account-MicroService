package com.amyojiakor.AccountMicroService.models.payloads;

import com.amyojiakor.AccountMicroService.models.enums.AccountType;
import com.amyojiakor.AccountMicroService.models.enums.CurrencyCode;

import java.math.BigDecimal;

public record AccountResponse (String accountNumber, String accountName, AccountType accountType, CurrencyCode currencyCode, BigDecimal accountBalance) {


}
