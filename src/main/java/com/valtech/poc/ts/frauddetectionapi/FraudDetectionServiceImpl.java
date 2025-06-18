package com.valtech.poc.ts.frauddetectionapi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.valtech.poc.dto.FraudCheckResponseDTO;
import com.valtech.poc.dto.TransactionDTO;
import com.valtech.poc.ts.exception.FraudDetectionException;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@Service
public class FraudDetectionServiceImpl {

    private static final Logger logger = LoggerFactory.getLogger(FraudDetectionServiceImpl.class);

    @Autowired
    private FraudDetectionServiceClient fraudDetectionServiceClient;

    @CircuitBreaker(name = "default", fallbackMethod = "customFraudDetectionFallbackReponse")
    public FraudCheckResponseDTO fraudDetectionApi(TransactionDTO tdto) {
        FraudCheckResponseDTO fraudCheckResponseDTO = null;
        System.err.println("Initiated fraudDetection api: " + tdto);

        fraudCheckResponseDTO = fraudDetectionServiceClient.getFraudDetectionResponse(tdto);

        if (fraudCheckResponseDTO == null) {
            logger.debug("fraudCheckResponseDTO is null");
        } else {
            logger.debug("fraudCheckResponseDTO risk score: " + fraudCheckResponseDTO.getRiskScore());
        }

        return fraudCheckResponseDTO;
    }

    public FraudCheckResponseDTO customFraudDetectionFallbackReponse(TransactionDTO tdto, Exception ex) {
        System.out.println("Inside customFraudDetectionFallbackReponse due to exception: " + ex.getMessage());
        throw new FraudDetectionException("Fraud Detection Service is Down; " + ex.getMessage());
    }
}
