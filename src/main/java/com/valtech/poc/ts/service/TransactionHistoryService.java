package com.valtech.poc.ts.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import com.valtech.poc.entities.TransStatusHistory;
import com.valtech.poc.ts.repository.TransStatusHistoryRepository;

@Service
public class TransactionHistoryService {

    private final TransStatusHistoryRepository transStatusHistoryRepository;
    private static final Logger logger = LoggerFactory.getLogger(TransStatusHistoryRepository.class);
    public TransactionHistoryService(TransStatusHistoryRepository transStatusHistoryRepository) {
        this.transStatusHistoryRepository = transStatusHistoryRepository;
    }
       public List<TransStatusHistory> getTransactionHistory(@RequestParam Long transactionId) {
        logger.debug("calling to get trasaction History by email id {}", transactionId);
         List<TransStatusHistory> allTransStatusHistory = transStatusHistoryRepository.findByTransactionId(transactionId);
         return allTransStatusHistory;
       }
}
