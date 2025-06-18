package com.valtech.poc.ts.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class TransactionException extends RuntimeException {
    public TransactionException(String message) {
        super(message);
    }
}
