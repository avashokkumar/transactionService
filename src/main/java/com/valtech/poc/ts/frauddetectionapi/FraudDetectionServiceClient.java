package com.valtech.poc.ts.frauddetectionapi;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import com.valtech.poc.dto.FraudCheckResponseDTO;
import com.valtech.poc.dto.TransactionDTO;


@FeignClient(name ="fraud-detection-service", url = "${fraud_detection_api}")
public interface FraudDetectionServiceClient {

    @PostMapping("${fraud_detection_path}")
    public FraudCheckResponseDTO getFraudDetectionResponse(TransactionDTO transactionDTO);
    
}
