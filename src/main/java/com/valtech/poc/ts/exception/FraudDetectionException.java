package com.valtech.poc.ts.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND)
public class FraudDetectionException extends RuntimeException {
    public FraudDetectionException(String message) {
        super(message);
    }
}
