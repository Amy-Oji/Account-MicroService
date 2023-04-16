package com.amyojiakor.AccountMicroService.models.entities;

import com.amyojiakor.AccountMicroService.models.enums.AccountType;
import com.amyojiakor.AccountMicroService.models.enums.CurrencyCode;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "accounts")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private java.lang.String accountNumber;
    private java.lang.String accountName;
    @Enumerated(value = EnumType.STRING)
    private AccountType accountType;
    @Enumerated(value = EnumType.STRING)
    private CurrencyCode currencyCode;
    private BigDecimal accountBalance;
}
