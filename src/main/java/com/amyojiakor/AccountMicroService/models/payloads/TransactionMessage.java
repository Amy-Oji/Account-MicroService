package com.amyojiakor.AccountMicroService.models.payloads;


import com.amyojiakor.AccountMicroService.models.enums.TransactionType;

import java.math.BigDecimal;

public record TransactionMessage (String sourceAccountNumber, String recipientAccountNumber, TransactionType transactionType, BigDecimal amount, String referenceNumber){
}
