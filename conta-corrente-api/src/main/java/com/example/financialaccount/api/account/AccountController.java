package com.example.financialaccount.api.account;

import com.example.financialaccount.api.account.mapper.AccountResponseMapper;
import com.example.financialaccount.api.account.mapper.MovementResponseMapper;
import com.example.financialaccount.api.account.mapper.TransferResponseMapper;
import com.example.financialaccount.api.account.request.OpenAccountRequest;
import com.example.financialaccount.api.account.request.RegisterMovementRequest;
import com.example.financialaccount.api.account.request.TransferRequest;
import com.example.financialaccount.api.account.response.AccountResponse;
import com.example.financialaccount.api.account.response.MovementResponse;
import com.example.financialaccount.api.account.response.TransferResponse;
import com.example.financialaccount.domain.dto.OpenAccountInput;
import com.example.financialaccount.domain.dto.RegisterMovementInput;
import com.example.financialaccount.domain.dto.TransferInput;
import com.example.financialaccount.domain.money.MonetaryAmount;
import com.example.financialaccount.domain.service.AccountMovementService;
import com.example.financialaccount.domain.service.AccountOpeningService;
import com.example.financialaccount.domain.service.TransferService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountOpeningService accountOpeningService;
    private final AccountMovementService accountMovementService;
    private final TransferService transferService;

    @PostMapping
    public ResponseEntity<AccountResponse> open(@Valid @RequestBody OpenAccountRequest request) {
        var account = accountOpeningService.open(new OpenAccountInput(request.identityNumber()));
        return ResponseEntity.status(HttpStatus.CREATED).body(AccountResponseMapper.toResponse(account));
    }

    @PostMapping("/{accountId}/deposits")
    public ResponseEntity<MovementResponse> deposit(
            @PathVariable UUID accountId,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody RegisterMovementRequest request) {
        var input = new RegisterMovementInput(accountId, new MonetaryAmount(request.amount()), idempotencyKey);
        var movement = accountMovementService.deposit(input);
        return ResponseEntity.status(HttpStatus.CREATED).body(MovementResponseMapper.toResponse(movement));
    }

    @PostMapping("/{accountId}/withdrawals")
    public ResponseEntity<MovementResponse> withdraw(
            @PathVariable UUID accountId,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody RegisterMovementRequest request) {
        var input = new RegisterMovementInput(accountId, new MonetaryAmount(request.amount()), idempotencyKey);
        var movement = accountMovementService.withdraw(input);
        return ResponseEntity.status(HttpStatus.CREATED).body(MovementResponseMapper.toResponse(movement));
    }

    @PostMapping("/{accountId}/transfers")
    public ResponseEntity<TransferResponse> transfer(
            @PathVariable UUID accountId,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody TransferRequest request) {
        var input = new TransferInput(
                accountId,
                request.destinationAccountId(),
                new MonetaryAmount(request.amount()),
                idempotencyKey);
        var transfer = transferService.transfer(input);
        return ResponseEntity.status(HttpStatus.CREATED).body(TransferResponseMapper.toResponse(transfer));
    }
}
