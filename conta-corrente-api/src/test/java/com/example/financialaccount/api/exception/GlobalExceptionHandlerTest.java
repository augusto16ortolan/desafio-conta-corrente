package com.example.financialaccount.api.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;
import com.example.financialaccount.domain.exception.AccountNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.resource.NoResourceFoundException;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

    @Test
    void returnsBadRequestForInvalidInput() {
        var response = exceptionHandler.handleInvalidInput(new IllegalArgumentException("Entrada inválida"));

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Entrada inválida", response.getBody().message());
    }

    @Test
    void returnsNotFoundForMissingResource() {
        var exception = new NoResourceFoundException(HttpMethod.GET, "/api/unknown", "Recurso não encontrado");

        var response = exceptionHandler.handleResourceNotFound(exception);

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void returnsNotFoundForMissingAccount() {
        var response = exceptionHandler.handleAccountNotFound(new AccountNotFoundException(UUID.randomUUID()));

        assertEquals(404, response.getStatusCode().value());
    }
}
