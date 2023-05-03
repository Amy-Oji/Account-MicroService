package com.amyojiakor.AccountMicroService.models.payloads;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransactionMessageResponse {
   private TransactionStatus transactionStatus;
   private String accountNum;
   private BigDecimal newAccountBalance;
   private String errorMessage;
}
