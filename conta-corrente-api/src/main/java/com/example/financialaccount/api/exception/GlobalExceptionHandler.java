package com.example.financialaccount.api.exception;

import com.example.financialaccount.domain.exception.AccountNotFoundException;
import com.example.financialaccount.domain.exception.AccountNumberGenerationException;
import com.example.financialaccount.domain.exception.DuplicateIdentityNumberException;
import com.example.financialaccount.domain.exception.InsufficientBalanceException;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateIdentityNumberException.class)
    ResponseEntity<ApiErrorResponse> handleDuplicateIdentityNumber(DuplicateIdentityNumberException exception) {
        return error(HttpStatus.CONFLICT, exception);
    }

    @ExceptionHandler(AccountNotFoundException.class)
    ResponseEntity<ApiErrorResponse> handleAccountNotFound(AccountNotFoundException exception) {
        return error(HttpStatus.NOT_FOUND, exception);
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    ResponseEntity<ApiErrorResponse> handleInsufficientBalance(InsufficientBalanceException exception) {
        return error(HttpStatus.UNPROCESSABLE_CONTENT, exception);
    }

    @ExceptionHandler(AccountNumberGenerationException.class)
    ResponseEntity<ApiErrorResponse> handleAccountNumberGeneration(AccountNumberGenerationException exception) {
        return error(HttpStatus.SERVICE_UNAVAILABLE, exception);
    }

    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentNotValidException.class, HandlerMethodValidationException.class})
    ResponseEntity<ApiErrorResponse> handleInvalidInput(Exception exception) {
        return error(HttpStatus.BAD_REQUEST, exception);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    ResponseEntity<ApiErrorResponse> handleResourceNotFound(NoResourceFoundException exception) {
        return error(HttpStatus.NOT_FOUND, exception);
    }

    private ResponseEntity<ApiErrorResponse> error(HttpStatus status, Exception exception) {
        var body = new ApiErrorResponse(Instant.now(), status.value(), status.getReasonPhrase(), exception.getMessage());
        return ResponseEntity.status(status).body(body);
    }
}
