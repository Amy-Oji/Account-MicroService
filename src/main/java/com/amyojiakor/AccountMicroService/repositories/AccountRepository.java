package com.amyojiakor.AccountMicroService.repositories;

import com.amyojiakor.AccountMicroService.models.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
}
