package com.valtech.poc.ts.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.valtech.poc.entities.AccountDetails;
import java.util.List;



public interface AccountRepository extends JpaRepository<AccountDetails, Long> {

    public AccountDetails findByAccountNo(Long accountNo);

    List<AccountDetails> findByUserEmail(String userEmail);

}
