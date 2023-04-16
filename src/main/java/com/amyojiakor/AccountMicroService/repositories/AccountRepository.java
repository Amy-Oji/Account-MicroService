package com.amyojiakor.AccountMicroService.repositories;

import com.amyojiakor.AccountMicroService.models.entities.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByAccountNumber(String accountNum);
}
