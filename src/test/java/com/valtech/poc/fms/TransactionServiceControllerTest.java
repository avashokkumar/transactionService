package com.valtech.poc.fms;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.valtech.poc.dto.TransactionDTO;
import com.valtech.poc.ts.controller.TransactionServiceController;
import com.valtech.poc.ts.exception.AccountException;
import com.valtech.poc.ts.exception.ErrorDetails;
import com.valtech.poc.ts.service.TransactionService;
import com.valtech.poc.ts.util.DtoMapper;
import com.valtech.poc.ts.util.MessageUtil;
import com.valtech.poc.ts.util.ValidateAccount;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(TransactionServiceController.class)
@ActiveProfiles("test")
public class TransactionServiceControllerTest {

       @Autowired
       private MockMvc mockMvc;

       @MockitoBean
       private DtoMapper dtoMapper;

       @MockitoBean
       private TransactionService transactionService;

       @MockitoBean
       private ValidateAccount validateAccount;

       private TransactionDTO req_TransactionDto;

       @Autowired
       private ObjectMapper objectMapper;

       @MockitoBean
       private MessageUtil messageUtil;

       @BeforeEach
       void setup() {
              req_TransactionDto = new TransactionDTO();
              req_TransactionDto.setTransFromAccNo(1234567890L);
              req_TransactionDto.setTransToAccNo(9876543210L);
              req_TransactionDto.setTransAmount(new BigDecimal("5000"));
              req_TransactionDto.setTransactionId(1L);
              req_TransactionDto.setTransStatus("pending");
              req_TransactionDto.setTransactionType("SWIFT");
              req_TransactionDto.setSourceIp("127.0.0.1");
              objectMapper = new ObjectMapper();
              objectMapper.registerModule(new JavaTimeModule());
       }

       @Test
       void transfer_Success_Allowed() throws Exception {
              TransactionDTO responseDto = req_TransactionDto;
              responseDto.setTransactionId(1l);
              responseDto.setTransStatus("A");
              Mockito.when(transactionService.initiateTransaction(Mockito.any(TransactionDTO.class)))
                            .thenReturn(responseDto);

              mockMvc.perform(post("/api/transactionservice/v1/transfer")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(req_TransactionDto)))
                            .andExpect(status().isOk())
                            .andExpect(jsonPath("$.transactionId").value(1l))
                            .andExpect(jsonPath("$.transStatus").value("A"));
       }

       @Test
       void transfer_success_manualProcessing() throws Exception {
              TransactionDTO responseDto = req_TransactionDto;
              responseDto.setTransactionId(1l);
              responseDto.setTransAmount(new BigDecimal(10000l));
              responseDto.setTransStatus("MP");
              Mockito.when(transactionService.initiateTransaction(Mockito.any(TransactionDTO.class)))
                            .thenReturn(responseDto);

              mockMvc.perform(post("/api/transactionservice/v1/transfer")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(req_TransactionDto)))
                            .andExpect(status().isOk())
                            .andExpect(jsonPath("$.transactionId").value(1L))
                            .andExpect(jsonPath("$.transStatus").value("MP"))
                            .andExpect(jsonPath("$.transAmount").value("10000"));
       }

       @Test
       void transfer_fail_Prohibited() throws Exception {
              req_TransactionDto.setDestCountryCode("PAK");
              TransactionDTO responseDto = req_TransactionDto;
              responseDto.setTransactionId(1l);
              responseDto.setTransStatus("P");
              responseDto.setSourceCountryCode("PAK");
              Mockito.when(transactionService.initiateTransaction(Mockito.any(TransactionDTO.class)))
                            .thenReturn(responseDto);

              mockMvc.perform(post("/api/transactionservice/v1/transfer")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(req_TransactionDto)))
                            .andExpect(status().isOk())
                            .andExpect(jsonPath("$.transactionId").value(1L))
                            .andExpect(jsonPath("$.transStatus").value("P"))
                            .andExpect(jsonPath("$.sourceCountryCode").value("PAK"));
       }

       @Test
       void transfer_accountException_shouldReturn400() throws Exception {
              req_TransactionDto.setTransFromAccNo(1l);
              ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(),
                            "Sender account not found:", "");
              Mockito.when(transactionService.initiateTransaction(Mockito.any(TransactionDTO.class)))
                            .thenThrow(new AccountException("Invalid account"));
              mockMvc.perform(post("/api/transactionservice/v1/transfer")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(errorDetails)))
                            .andExpect(status().isBadRequest());
       }

       @Test
       void transfer_runtimeException_shouldReturn404() throws Exception {
              Mockito.when(transactionService.initiateTransaction(Mockito.any(TransactionDTO.class)))
                            .thenThrow(new RuntimeException("Unknown error"));
              mockMvc.perform(post("/api/transactionservice/v1/transfer")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(req_TransactionDto)))
                            .andExpect(status().isNotFound());
       }


//    @Test
//     void transfer_complete_manualProcessing_allowed() throws Exception {
//         TransactionDTO inputDto = new TransactionDTO();
//         inputDto.setTransactionId(Long.valueOf(104));
//         inputDto.setTransStatus("MP");
//         TransactionDTO fetchedDto = new TransactionDTO();
//         fetchedDto.setTransactionId(Long.valueOf(104));
//         fetchedDto.setTransStatus("MP");
//         Mockito.when(transactionService.getTransactionMst(Long.valueOf(104))).thenReturn(fetchedDto);
//         TransactionDTO resultDto = new TransactionDTO();
//         resultDto.setTransactionId(Long.valueOf(104));
//         resultDto.setTransStatus("A");
//         Mockito.when(transactionService.finishTransfer(fetchedDto.getTransactionId(),resultDto.getTransStatus())).thenReturn(resultDto);

//         mockMvc.perform(MockMvcRequestBuilders.post("/api/transactionservice/v1/completetransfer")
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content(objectMapper.writeValueAsString(inputDto)))
//                 .andDo(print())
//                 .andExpect(status().)
//                 .andExpect(jsonPath("$.transactionId").value(104l))
//                 .andExpect(jsonPath("$.transStatus").value("A"));
//     }

    @Test
    void transfer_complete_manualProcessing_notFound() throws Exception {
        TransactionDTO inputDto = new TransactionDTO();
        inputDto.setTransactionId(Long.valueOf(104));
        Mockito.when(transactionService.getTransactionMst(inputDto.getTransactionId())).thenReturn(null);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/transactionservice/v1/completetransfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isNotFound()); // because you're throwing TransactionException
    }


    @Test
    void transfer_complete_manualProcessing_alreadyProcessed() throws Exception {
        TransactionDTO inputDto = new TransactionDTO();
        inputDto.setTransactionId(Long.valueOf(104));
        TransactionDTO fetchedDto = new TransactionDTO();
        inputDto.setTransactionId(Long.valueOf(104));
        fetchedDto.setTransStatus("A");
        Mockito.when(transactionService.getTransactionMst(inputDto.getTransactionId())).thenReturn(fetchedDto);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/transactionservice/v1/completetransfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isBadRequest());
    }


    @Test
    void transfer_complete_manualProcessing_prohibited() throws Exception {
       TransactionDTO inputDto = new TransactionDTO();
       inputDto.setTransactionId(Long.valueOf(104));

        TransactionDTO fetchedDto = new TransactionDTO();
        fetchedDto.setTransactionId(Long.valueOf(104));
        fetchedDto.setTransStatus("A");

        TransactionDTO afterTransfer = new TransactionDTO();
        afterTransfer.setTransactionId(Long.valueOf(104));
        afterTransfer.setTransStatus("P");

        Mockito.when(transactionService.getTransactionMst(inputDto.getTransactionId())).thenReturn(fetchedDto);
        Mockito.when(transactionService.finishTransfer(inputDto.getTransactionId(),afterTransfer.getTransStatus())).thenReturn(afterTransfer);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/transactionservice/v1/completetransfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                        .andDo(print())
                        .andExpect(status().isBadRequest());
    }

}