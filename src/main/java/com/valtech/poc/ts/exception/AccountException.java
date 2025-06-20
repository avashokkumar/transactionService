package com.valtech.poc.ts.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND)
public class AccountException extends RuntimeException {
    public AccountException(String message) {
        super(message);
    }
}
