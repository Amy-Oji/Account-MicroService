package com.amyojiakor.AccountMicroService.models.payloads;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransactionMessageResponse {
   private TransactionStatus status;
   private String sourceAccountNumber;
   private String recipientAccountNumber;
   private BigDecimal newAccountBalance;
   private String referenceNumber;
   private String errorMessage;
}
