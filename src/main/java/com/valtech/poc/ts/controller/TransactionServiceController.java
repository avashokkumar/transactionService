package com.valtech.poc.ts.controller;

import com.valtech.poc.dto.TransactionDTO;
import com.valtech.poc.entities.AccountDetails;
import com.valtech.poc.ts.exception.AccountException;
import com.valtech.poc.ts.exception.ErrorDetails;
import com.valtech.poc.ts.exception.FraudDetectionException;
import com.valtech.poc.ts.exception.TransactionException;
import com.valtech.poc.ts.exception.TransactionNotFoundException;
import com.valtech.poc.ts.service.TransactionService;
import com.valtech.poc.ts.util.MessageUtil;
import com.valtech.poc.ts.util.ValidateAccount;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

@RestController
@RequestMapping("/api/transactionservice/v1")
public class TransactionServiceController {

    private final TransactionService transactionService;


    @Autowired
    private MessageUtil messageUtil;


    private static final Logger logger = LoggerFactory.getLogger(TransactionServiceController.class);

    public TransactionServiceController(TransactionService transactionService, ValidateAccount validateAccount) {
        this.transactionService = transactionService;
    }

    @Operation(
        summary = "Initiate Transaction, Validate Accounts and fraud detection and Complete the transaction",
        tags = {"1. Initiates a fund transfer"}
    )
    @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Transaction successful",
    content = @Content(schema = @Schema(implementation = TransactionDTO.class))),
    @ApiResponse(responseCode = "400", description = "Same account transfer is not allowed | Account Is In-Active | Insufficent Funds",  
    content = @Content(schema = @Schema(implementation = ErrorDetails.class))),
    @ApiResponse(responseCode = "404", description = "Transaction could not be processed | Transaction Prohibited",  
    content = @Content(schema = @Schema(implementation = ErrorDetails.class))),
})
    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(@RequestBody TransactionDTO tdo) {
        try {
        TransactionDTO transactionDto = transactionService.initiateTransaction(tdo);
        System.err.println("transactionComplete " +transactionDto);
        return ResponseEntity.ok(transactionDto);
        } catch(AccountException anf) {
            throw new AccountException(anf.getMessage());
        } catch(FraudDetectionException ex){
            throw new FraudDetectionException(ex.getMessage());
         } catch(RuntimeException ex){
           return ResponseEntity.status(404).body(ex.getMessage());
        }
    }

    @Operation(
        summary = "Validate and complete the \\\"Manual processing\\\" Transaction only",
        tags = {"2. Completes a Manual processing Transaction"}
    )
    @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Transaction successful",
    content = @Content(schema = @Schema(implementation = TransactionDTO.class))),
    @ApiResponse(responseCode = "400", description = "Same account transfer is not allowed | Account Is In-Active | Insufficent Funds",  
    content = @Content(schema = @Schema(implementation = ErrorDetails.class))),
    @ApiResponse(responseCode = "404", description = "Transaction could not be processed | Transaction Prohibited",  
    content = @Content(schema = @Schema(implementation = ErrorDetails.class))),
})
    @PostMapping("/completetransfer")
    public ResponseEntity<?> completetransfer(@RequestBody TransactionDTO tdo) {
        try {
         TransactionDTO transIdDto = transactionService.getTransactionMst(tdo.getTransactionId());
         if (transIdDto == null) {
            throw new TransactionNotFoundException(messageUtil.getMessage("exception.transaction.id.not.found",tdo.getTransactionId()));
        } else if ("A".equals(transIdDto.getTransStatus())) {  // null-safe check
            throw new TransactionException(messageUtil.getMessage("exception.transaction.already.processed", tdo.getTransactionId()));
        } else if("MP".equals(transIdDto.getTransStatus()) && (!StringUtils.isEmpty(tdo.getTransStatus()) && tdo.getTransStatus().equals("P") || tdo.getTransStatus().equals("A") ) ) {
            TransactionDTO transactionDto = transactionService.finishTransfer(tdo.getTransactionId(),tdo.getTransStatus()); 
             return ResponseEntity.ok(transactionDto);
        }
        else {
             throw new TransactionException(messageUtil.getMessage("transaction.invalid.details", tdo.getTransactionId()));
         }
        } catch(TransactionNotFoundException anf) {
            throw new TransactionNotFoundException(anf.getMessage());
        }
        catch(TransactionException anf) {
            throw new TransactionException(anf.getMessage());
        } catch(RuntimeException ex){
            logger.debug("RuntimeException---->" +ExceptionUtils.getStackTrace(ex));
           return ResponseEntity.status(404).body(ex.getMessage());
        } catch(Exception ex){
            logger.debug("Exception---->" +ExceptionUtils.getStackTrace(ex));
           return ResponseEntity.status(404).body(ex.getMessage());
        }
    }

    @Operation(
        summary = "Get Account Balance by accountNo",
        tags = {"5. Account balance"}
    )
    @GetMapping("/accountChecking")
    public ResponseEntity<?> accountChecking(@RequestParam Long accountNo) {
            AccountDetails accountDetails = transactionService.checkAccount(accountNo);
            if(accountDetails != null)
              return ResponseEntity.ok(accountDetails.getCurrentBalance());
              logger.debug("in account checking and returning");
           throw new AccountException("account NOt Found");
            // return null;
    }
}
