package com.magadiflo.app.repositories;

import com.magadiflo.app.models.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IAccountRepository extends JpaRepository<Account, Long> {

}
