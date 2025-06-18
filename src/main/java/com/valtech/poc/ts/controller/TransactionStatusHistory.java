package com.valtech.poc.ts.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.valtech.poc.entities.TransStatusHistory;
import com.valtech.poc.entities.TransactionMst;
import com.valtech.poc.ts.exception.ErrorDetails;
import com.valtech.poc.ts.service.TransactionHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;


@RestController
@RequestMapping ("/api/transactionstatus/v1")
public class TransactionStatusHistory {

    private TransactionHistoryService transactionHistoryService;

    public TransactionStatusHistory(TransactionHistoryService transactionHistoryService) {
        this.transactionHistoryService = transactionHistoryService;
    }

    @Operation(
        summary = "Get Transaction History",
        tags = {"4. Get Transactions by Transaction Id"}
    )
    @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Fetched Successfully",
    content = @Content(schema = @Schema(implementation = TransactionMst.class))),
    @ApiResponse(responseCode = "404", description = "TransactionId Id not Found",  
    content = @Content(schema = @Schema(implementation = ErrorDetails.class))),
})
    @GetMapping("/gettransactionhistory")
    public List<TransStatusHistory> getTransactionHistory(@RequestParam Long transactionId) {
       return transactionHistoryService.getTransactionHistory(transactionId);
    }
}
