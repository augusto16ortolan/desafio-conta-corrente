package com.example.financialaccount.api.account;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.financialaccount.api.exception.GlobalExceptionHandler;
import com.example.financialaccount.domain.account.AccountNumber;
import com.example.financialaccount.domain.dto.AccountDto;
import com.example.financialaccount.domain.dto.AccountMovementDto;
import com.example.financialaccount.domain.exception.DuplicateIdentityNumberException;
import com.example.financialaccount.domain.identitynumber.IdentityNumber;
import com.example.financialaccount.domain.movement.MovementType;
import com.example.financialaccount.domain.service.AccountMovementService;
import com.example.financialaccount.domain.service.AccountOpeningService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

    @Mock
    private AccountOpeningService accountOpeningService;

    @Mock
    private AccountMovementService accountMovementService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AccountController(accountOpeningService, accountMovementService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void opensAccountAndReturnsCreatedResponse() throws Exception {
        var accountId = UUID.randomUUID();
        when(accountOpeningService.open(any())).thenReturn(new AccountDto(
                accountId,
                IdentityNumber.from("529.982.247-25"),
                AccountNumber.from("0000000001"),
                new BigDecimal("0.00"),
                Instant.parse("2026-10-23T18:00:00Z")));

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identityNumber\":\"529.982.247-25\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.identityNumber").value("52998224725"))
                .andExpect(jsonPath("$.balance").value(0));
    }

    @Test
    void rejectsRequestWithoutIdentityNumber() throws Exception {
        mockMvc.perform(post("/api/accounts").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returnsConflictForDuplicateIdentityNumber() throws Exception {
        when(accountOpeningService.open(any())).thenThrow(new DuplicateIdentityNumberException());

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identityNumber\":\"529.982.247-25\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Já existe uma conta vinculada a este número de identificação."));
    }

    @Test
    void depositsWithIdempotencyKey() throws Exception {
        var accountId = UUID.randomUUID();
        var movementId = UUID.randomUUID();
        when(accountMovementService.deposit(any())).thenReturn(new AccountMovementDto(
                movementId,
                accountId,
                MovementType.DEPOSIT,
                new BigDecimal("15.00"),
                new BigDecimal("15.00"),
                Instant.parse("2026-10-23T18:00:00Z")));

        mockMvc.perform(post("/api/accounts/{accountId}/deposits", accountId)
                        .header("Idempotency-Key", "deposit-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":15.00}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("DEPOSIT"))
                .andExpect(jsonPath("$.balanceAfter").value(15));
    }
}
