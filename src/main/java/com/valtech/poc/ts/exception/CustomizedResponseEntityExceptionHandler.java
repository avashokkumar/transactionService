package com.valtech.poc.ts.exception;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;


@ControllerAdvice
public class CustomizedResponseEntityExceptionHandler extends ResponseEntityExceptionHandler{

	@ExceptionHandler(Exception.class)
	public final ResponseEntity<ErrorDetails> handleAllExceptions(Exception ex, WebRequest request) throws Exception {
		logger.warn("Exception---->");
		ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), 
				ex.getMessage(), request.getDescription(false));
		return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
		
	}

	@ExceptionHandler(TransactionException.class)
	public final ResponseEntity<ErrorDetails> handleTransactionException(Exception ex, WebRequest request) throws Exception {
		logger.warn("Transaction Exception----> "+ex.getMessage());
		ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), 
				ex.getMessage(), request.getDescription(false));
		return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
		
	}

	@ExceptionHandler(TransactionNotFoundException.class)
	public final ResponseEntity<ErrorDetails> handleTransactionNotFoundException(Exception ex, WebRequest request) throws Exception {
		logger.warn("TransactionNotFoundException ----> "+ex.getMessage());
		ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), 
				ex.getMessage(), request.getDescription(false));
		return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
		
	}

	

	@ExceptionHandler(AccountException.class)
	public final ResponseEntity<ErrorDetails> handleAccountException(Exception ex, WebRequest request) throws Exception {
		logger.warn("handleAccountException----> " +ex.getMessage());
		ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), 
				ex.getMessage(), request.getDescription(false));
		return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
		
	}

	@ExceptionHandler(FraudDetectionException.class)
	public final ResponseEntity<ErrorDetails> handleFraudDetectionException(Exception ex, WebRequest request) throws Exception {
		logger.warn("handleFraudDetectionException----> " +ex.getMessage());
		ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), 
				ex.getMessage(), request.getDescription(false));
		return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
		
	}
	
}