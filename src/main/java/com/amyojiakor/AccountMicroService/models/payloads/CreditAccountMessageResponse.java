package com.amyojiakor.AccountMicroService.models.payloads;

import com.amyojiakor.AccountMicroService.models.enums.CurrencyCode;
import com.amyojiakor.AccountMicroService.models.enums.TransactionStatus;
import com.amyojiakor.AccountMicroService.models.enums.TransactionType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Data
@NoArgsConstructor
public class CreditAccountMessageResponse {
    private String recipientAccountNumber;
    private TransactionType transactionType;
    private CurrencyCode currencyCode;
    private BigDecimal amount;
    private String referenceNumber;
    private BigDecimal recipientAccountNumberNewBal;
    private TransactionStatus status;
}
