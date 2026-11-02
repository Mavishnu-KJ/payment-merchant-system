package com.example.merchantservice.controller;

import com.example.merchantservice.model.dto.AmountRequestDto;
import com.example.merchantservice.model.dto.BalanceResponseDto;
import com.example.merchantservice.model.dto.MerchantRegisterRequestDto;
import com.example.merchantservice.model.dto.MerchantResponseDto;
import com.example.merchantservice.service.MerchantLockService;
import com.example.merchantservice.service.MerchantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/merchants")
@RequiredArgsConstructor
public class MerchantController {

    private final MerchantService merchantService;
    private final MerchantLockService merchantLockService;

    @PostMapping("/register")
    public ResponseEntity<MerchantResponseDto> register(@Valid @RequestBody MerchantRegisterRequestDto requestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(merchantService.register(requestDto));
    }

    @GetMapping("/{merchantId}")
    public ResponseEntity<MerchantResponseDto> getById(@PathVariable Long merchantId) {
        return ResponseEntity.ok(merchantService.getById(merchantId));
    }

    @PostMapping("/{merchantId}/credit")
    public ResponseEntity<MerchantResponseDto> credit(@PathVariable Long merchantId,
                                                      @Valid @RequestBody AmountRequestDto requestDto) {
        return ResponseEntity.ok(merchantService.credit(merchantId, requestDto));
    }

    @PostMapping("/{merchantId}/debit")
    public ResponseEntity<MerchantResponseDto> debit(@PathVariable Long merchantId,
                                                     @Valid @RequestBody AmountRequestDto requestDto) {
        return ResponseEntity.ok(merchantService.debit(merchantId, requestDto));
    }

    @GetMapping("/{merchantId}/balance")
    public ResponseEntity<BalanceResponseDto> getBalance(@PathVariable Long merchantId) {
        return ResponseEntity.ok(merchantService.getBalance(merchantId));
    }

    // Internal endpoints are used by transaction-service for payment orchestration.
    @GetMapping("/internal/{merchantId}")
    public ResponseEntity<MerchantResponseDto> internalGetById(@PathVariable Long merchantId) {
        return ResponseEntity.ok(merchantService.getById(merchantId));
    }

    @GetMapping("/internal/user/{userId}")
    public ResponseEntity<MerchantResponseDto> internalGetByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(merchantService.getByUserId(userId));
    }

    @PostMapping("/internal/{merchantId}/credit")
    public ResponseEntity<MerchantResponseDto> internalCredit(@PathVariable Long merchantId,
                                                              @RequestBody BigDecimal amount) {
        AmountRequestDto requestDto = new AmountRequestDto();
        requestDto.setAmount(amount);
        return ResponseEntity.ok(merchantService.credit(merchantId, requestDto));
    }

    @PostMapping("/internal/{merchantId}/debit")
    public ResponseEntity<MerchantResponseDto> internalDebit(@PathVariable Long merchantId,
                                                             @RequestBody BigDecimal amount) {
        AmountRequestDto requestDto = new AmountRequestDto();
        requestDto.setAmount(amount);
        return ResponseEntity.ok(merchantService.debit(merchantId, requestDto));
    }

    @GetMapping("/internal/{merchantId}/balance")
    public ResponseEntity<BalanceResponseDto> internalBalance(@PathVariable Long merchantId) {
        return ResponseEntity.ok(merchantService.getBalance(merchantId));
    }

    @PostMapping("/internal/{merchantId}/acquireLock")
    public ResponseEntity<Boolean> internalAcquireLock(@PathVariable Long merchantId,
                                                       @RequestParam(defaultValue = "60") long timeoutSeconds) {
        return ResponseEntity.ok(merchantLockService.acquireLock(merchantId));
    }

    @PostMapping("/internal/{merchantId}/releaseLock")
    public ResponseEntity<Void> internalReleaseLock(@PathVariable Long merchantId) {
        merchantLockService.releaseLock(merchantId);
        return ResponseEntity.ok().build();
    }
}
