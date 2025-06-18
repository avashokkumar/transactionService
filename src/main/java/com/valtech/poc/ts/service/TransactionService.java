package com.valtech.poc.ts.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.valtech.poc.dto.FraudCheckResponseDTO;
import com.valtech.poc.dto.TransactionDTO;
import com.valtech.poc.entities.AccountDetails;
import com.valtech.poc.entities.TransStatusHistory;
import com.valtech.poc.entities.TransactionMst;
import com.valtech.poc.ts.exception.AccountException;
import com.valtech.poc.ts.frauddetectionapi.FraudDetectionServiceImpl;
import com.valtech.poc.ts.repository.AccountRepository;
import com.valtech.poc.ts.repository.TransStatusHistoryRepository;
import com.valtech.poc.ts.repository.TransactionRepository;
import com.valtech.poc.ts.util.DtoMapper;
import com.valtech.poc.ts.util.MessageUtil;
import com.valtech.poc.ts.util.ValidateAccount;

@Service
public class TransactionService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    @Autowired
    private MessageUtil messageUtil;

    @Autowired
    private DtoMapper dtoMapper;

    @Autowired
    private FraudDetectionServiceImpl fraudDetectionServiceImpl;

    private final AccountRepository accountRepo;
    private final TransactionRepository transactionRepo;
    private final TransStatusHistoryRepository transactionHistoryRepo;
    private final ValidateAccount validateAccount;
   

    public TransactionService(AccountRepository accountRepo,
                              TransactionRepository transactionRepo,
                              TransStatusHistoryRepository transactionHistoryRepo,
                              ValidateAccount validateAccount) {
        this.accountRepo = accountRepo;
        this.transactionRepo = transactionRepo;
        this.transactionHistoryRepo = transactionHistoryRepo;
        this.validateAccount = validateAccount;
    }

    @Transactional
    public TransactionMst validateTransaction(TransactionMst pendingTransaction, FraudCheckResponseDTO fraudResponse) {
        logger.debug("Validating transaction: {}", pendingTransaction);

        Long fromAccountNo = pendingTransaction.getFromAccountNo();
        Long toAccountNo = pendingTransaction.getToAccountNo();
        BigDecimal amount = pendingTransaction.getTransactionAmount();
        LocalDateTime now = LocalDateTime.now();
        String fraudStatus = fraudResponse.getStatus();
        String riskReason = fraudResponse.getRiskReason();
        String allowedStatus = messageUtil.getMessage("transaction.Allowed.code");

        AccountDetails senderAccount = accountRepo.findByAccountNo(fromAccountNo);
        AccountDetails receiverAccount = accountRepo.findByAccountNo(toAccountNo);

        TransStatusHistory history = new TransStatusHistory();
        history.setTransactionId(pendingTransaction.getTransactionId());
        history.setCreatedDate(now);
        pendingTransaction.setTransactionDate(now);

        try {
            if (fraudStatus.equals(allowedStatus)) {
                validateAccount.validateAccount(senderAccount, amount, "from", fromAccountNo);
                validateAccount.validateAccount(receiverAccount, BigDecimal.ZERO, "to", toAccountNo);

                senderAccount.setCurrentBalance(senderAccount.getCurrentBalance().subtract(amount));
                receiverAccount.setCurrentBalance(receiverAccount.getCurrentBalance().add(amount));

                accountRepo.save(senderAccount);
                accountRepo.save(receiverAccount);

                history.setComments(messageUtil.getMessage("transaction.completed.success"));
            } else {
                history.setComments(riskReason);
            }

            history.setTransStatus(fraudStatus);
            pendingTransaction.setTransactionStatus(fraudStatus);
            transactionHistoryRepo.save(history);
        } catch (Exception e) {
            String failStatus = messageUtil.getMessage("transaction.status.fail");
            logger.error("Transaction failed: {}", e.getMessage(), e);

            history.setComments(e.getMessage());
            history.setTransStatus(failStatus);
            transactionHistoryRepo.save(history);

            pendingTransaction.setTransactionStatus(failStatus);
        }

        return pendingTransaction;
    }

    @Transactional
    public TransactionDTO initiateTransaction(TransactionDTO dto) {

        LocalDateTime now = LocalDateTime.now();
        dto.setTransactionDate(now);
        logger.debug("Initiating transaction: {}" +dto);
        try {
            TransactionMst transaction = dtoMapper.toTMst(dto);
            logger.debug("Initiating transaction check null {0}" +dto);
            if(transaction == null)
               throw new AccountException(messageUtil.getMessage("transaction.invalid.details", dto));
            Long fromAcc = dto.getTransFromAccNo();
            Long toAcc = dto.getTransToAccNo();
            AccountDetails sender = accountRepo.findByAccountNo(fromAcc);
            if (sender == null) {
                throw new AccountException(messageUtil.getMessage("sender.account.not.found", String.valueOf(fromAcc)));
            }

            AccountDetails receiver = accountRepo.findByAccountNo(toAcc);
            if (receiver == null) {
                throw new AccountException(messageUtil.getMessage("receiver.account.not.found", String.valueOf(toAcc)));
            }

            if (fromAcc.equals(toAcc)) {
                throw new AccountException(messageUtil.getMessage("same.account.transfer.not.allowed", String.valueOf(dto.getTransFromAccNo())));
            }
            
            transaction.setTransactionStatus(messageUtil.getMessage("transaction.status.pending"));
            TransactionMst savedTransaction = transactionRepo.save(transaction);

            logger.debug("Saved transaction ID: {}", savedTransaction.getTransactionId());

            TransStatusHistory history = new TransStatusHistory();
            history.setTransactionId(savedTransaction.getTransactionId());
            history.setComments(messageUtil.getMessage("transaction.initatited"));
            history.setTransStatus(dto.getTransStatus());
            history.setCreatedDate(now);
            transactionHistoryRepo.save(history);
            TransactionDTO responseDto = dtoMapper.toTdto(savedTransaction);
            // String transactionType = "MP";
            // Map<String, String> entry = new HashMap<String,String>();
	        //  entry.put("P", "Prohibited");
            // entry.put("A", "Transaction Allowed");
            // entry.put("MP", "Manual Procession");
            // FraudCheckResponseDTO fraudResponse = new FraudCheckResponseDTO("1", transactionType, "A", entry.get(transactionType));
            FraudCheckResponseDTO fraudResponse = fraudDetectionServiceImpl.fraudDetectionApi(responseDto);
            TransactionMst finalTransaction = validateTransaction(savedTransaction, fraudResponse);
            return dtoMapper.toTdto(finalTransaction);
        } catch (AccountException ae) {
            logger.error("Account validation failed: {}", ae.getMessage(), ae);
            throw ae;
        } catch (Exception e) {
            logger.error("Unexpected error during transaction: {}", e.getMessage(), e);
            throw e;
        }
    }


    @Transactional
    public  TransactionDTO  finishTransfer(long TransactionId, String transferStatus) {
        logger.debug("Transaction DTO received: {}", TransactionId);
        LocalDateTime localDateTime = LocalDateTime.now();
        TransactionMst transactionMst = null;
        TransStatusHistory updateTsh = new TransStatusHistory();
        final String allowed = messageUtil.getMessage("transaction.Allowed.code");
        final String prohibited = messageUtil.getMessage("transaction.prohibited.code");
        try {
         updateTsh.setTransactionId(TransactionId);
         updateTsh.setCreatedDate(localDateTime);
         Optional<TransactionMst> optionalTmst = transactionRepo.findById(TransactionId);
         if(optionalTmst.isPresent()) {
            transactionMst = optionalTmst.get();
            // List<TransStatusHistory> listTsh = tshRepo.findByTransactionId(tsd.getTransactionId());
            String status = transactionMst.getTransactionStatus();
           if(transferStatus.equals("P")) {
                logger.debug(" Transaction Rejected By Admin");
                updateTsh.setTransStatus(prohibited);
                updateTsh.setComments(messageUtil.getMessage("transaction.admin.rejected"));
                transactionHistoryRepo.save(updateTsh);
                transactionMst.setTransactionDate(localDateTime);
                transactionMst.setTransactionStatus(prohibited);
                transactionRepo.save(transactionMst);
            }  else if(status.equals("MP")) {
                logger.debug(" has mnaul processing");
                final Long fromAccountNo = transactionMst.getFromAccountNo();
                final Long toAccountNo = transactionMst.getToAccountNo();
                final BigDecimal amount = transactionMst.getTransactionAmount();
                AccountDetails senderAccount = accountRepo.findByAccountNo(fromAccountNo);
                AccountDetails receiverAccount = accountRepo.findByAccountNo(toAccountNo);
                senderAccount.setCurrentBalance(senderAccount.getCurrentBalance().subtract(amount));
                receiverAccount.setCurrentBalance(receiverAccount.getCurrentBalance().add(amount));
                // Save updated accounts
                accountRepo.save(senderAccount);
                accountRepo.save(receiverAccount);
                transactionMst.setTransactionDate(localDateTime);
                transactionMst.setTransactionStatus(allowed);
                transactionRepo.save(transactionMst);
                updateTsh.setTransStatus(allowed);
                updateTsh.setComments(messageUtil.getMessage("transaction.completed.success"));
                transactionHistoryRepo.save(updateTsh);
            } else {
                logger.debug(" No manaul processing");
                updateTsh.setTransStatus(prohibited);
                updateTsh.setComments(messageUtil.getMessage("transaction.nosuch.mp.available"));
                transactionHistoryRepo.save(updateTsh);
            }
           }
          return  dtoMapper.toTdto(transactionMst);
        } 
         catch (Exception e) {
            logger.error("Unexpected error during transfer: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
        public TransactionDTO getTransactionMst(long transactionId) {
            Optional<TransactionMst> optionaltransactionMst = transactionRepo.findById(transactionId);
            if(optionaltransactionMst.isPresent()) 
                  return dtoMapper.toTdto(optionaltransactionMst.get());
            return null;
           }

    @Transactional(readOnly = true)
    public AccountDetails getAccountDetails(Long accountNo) {
        AccountDetails accountDetails = accountRepo.findByAccountNo(accountNo);

        if (accountDetails == null) {
            logger.warn(messageUtil.getMessage("account.not.found", String.valueOf(accountNo)));
            throw new AccountException(messageUtil.getMessage("account.not.found", String.valueOf(accountNo)));
        }

        return accountDetails;
    }

    @Transactional(readOnly = true)
    public AccountDetails checkAccount(Long accountNo) {
        AccountDetails accountDetails = accountRepo.findByAccountNo(accountNo);
        if (accountDetails == null) {
            logger.warn(messageUtil.getMessage("account.not.found", String.valueOf(accountNo)));
            // throw new
            // AccountNotFoundException(messageUtil.getMessage("account.not.found",
            // String.valueOf(accountNo)));
        }
        return accountDetails;
    }

    @Transactional(readOnly = true)
    public List<TransactionMst> getAllTransactions(List<Long> accountNos) {
           return transactionRepo.findStatementByAccountIds(accountNos);
    }
}
