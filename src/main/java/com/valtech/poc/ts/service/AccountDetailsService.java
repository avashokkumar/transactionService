package com.valtech.poc.ts.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import com.valtech.poc.entities.AccountDetails;
import com.valtech.poc.entities.TransactionMst;
import com.valtech.poc.ts.exception.AccountException;
import com.valtech.poc.ts.repository.AccountRepository;
import com.valtech.poc.ts.util.MessageUtil;

@Service
public class AccountDetailsService {

    private final AccountRepository accountRepository;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private MessageUtil messageUtil;

    private static final Logger logger = LoggerFactory.getLogger(AccountDetailsService.class);
    public AccountDetailsService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }
       public List<TransactionMst> getAccountStatement(@RequestParam String emailId) {
        logger.debug("calling to get getAccountStatement History {}", emailId);
         
        List<AccountDetails> accountDetailsList = accountRepository.findByUserEmail(emailId);
        logger.debug("accountDetailsList-- {}" +accountDetailsList);
        if(accountDetailsList.isEmpty()) {
            throw new AccountException(messageUtil.getMessage("account.email.id.not.found", emailId));
        }

        List<Long> accountNoList = accountDetailsList.stream()
        .map(AccountDetails::getAccountNo) // assuming getAccountNo() returns the account number
        .collect(Collectors.toList());

        logger.debug("accountNoList {}", accountNoList);

         return transactionService.getAllTransactions(accountNoList);
       }
}
