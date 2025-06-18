package com.valtech.poc.ts.controller;

import java.util.List;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.valtech.poc.entities.TransactionMst;
import com.valtech.poc.ts.exception.AccountException;
import com.valtech.poc.ts.exception.ErrorDetails;
import com.valtech.poc.ts.service.AccountDetailsService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;


@RestController
@RequestMapping ("/api/account/v1")
public class AccountHistory {

    private AccountDetailsService accountDetailsService;

    public AccountHistory(AccountDetailsService accountDetailsService) {
        this.accountDetailsService = accountDetailsService;
    }
    
    @Operation(
        summary = "Get User Transactions by Email-Id",
        tags = {"3. Account Transaction History "}
    )
    @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Transactions fetched successfully",
    content = @Content(array = @ArraySchema(schema = @Schema(implementation = TransactionMst.class)))),
    @ApiResponse(responseCode = "404", description = "Email Id not Found",  
    content = @Content(schema = @Schema(implementation = ErrorDetails.class))),
})

    @GetMapping("/getuserstatementbyemail")
    public ResponseEntity<?> getAccountStatement(@RequestParam String emailId) {
        try {
            List<TransactionMst> transactions = accountDetailsService.getAccountStatement(emailId);
              return ResponseEntity.ok(transactions);
        } catch (AccountException anf) {
                throw new AccountException(anf.getMessage());
        } catch(RuntimeException ex){
            return ResponseEntity.status(404).body(ex.getMessage());
         }
    }
  
}
