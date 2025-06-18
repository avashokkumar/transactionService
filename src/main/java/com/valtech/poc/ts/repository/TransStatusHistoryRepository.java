package com.valtech.poc.ts.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.valtech.poc.entities.TransStatusHistory;
import java.util.List;


public interface TransStatusHistoryRepository  extends JpaRepository<TransStatusHistory, Long> {

   List<TransStatusHistory> findByTransactionId(Long transactionId);

} 