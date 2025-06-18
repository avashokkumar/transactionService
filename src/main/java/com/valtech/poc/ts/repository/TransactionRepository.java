package com.valtech.poc.ts.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.valtech.poc.entities.TransactionMst;

import jakarta.transaction.Transactional;

public interface TransactionRepository extends JpaRepository<TransactionMst, Long> {
    
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("UPDATE TransactionMst t SET t.transactionStatus = :status WHERE t.transactionId = :id")
    int updateTransactionStatus(@Param("id") Long transactionId, @Param("status") String transactionStatus);


    @Query("SELECT a FROM TransactionMst a WHERE a.fromAccountNo IN :accounts")
     List<TransactionMst> findStatementByAccountIds(@Param("accounts") List<Long> accounts);
   
}
