package com.amyojiakor.AccountMicroService.models.payloads;

import lombok.Data;

@Data
public class TransactionMessageResponse {
   private TransactionStatus transactionStatus;
   private String errorMessage;
}
