package com.example.merchantservice.controller;

import com.example.merchantservice.model.dto.AmountRequestDto;
import com.example.merchantservice.model.dto.MerchantResponseDto;
import com.example.merchantservice.model.dto.WalletCompatResponseDto;
import com.example.merchantservice.service.MerchantLockService;
import com.example.merchantservice.service.MerchantService;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/api/wallets/internal")
@RequiredArgsConstructor
public class MerchantWalletCompatibilityController {

    private final MerchantService merchantService;
    private final MerchantLockService merchantLockService;

    @GetMapping("/{merchantId}")
    public ResponseEntity<WalletCompatResponseDto> getWalletById(@PathVariable Long merchantId) {
        MerchantResponseDto merchant = merchantService.getById(merchantId);
        return ResponseEntity.ok(toWalletCompat(merchant));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<WalletCompatResponseDto> getWalletByUserId(@PathVariable Long userId) {
        MerchantResponseDto merchant = merchantService.getByUserId(userId);
        return ResponseEntity.ok(toWalletCompat(merchant));
    }

    @PostMapping("/{merchantId}/debit")
    public ResponseEntity<WalletCompatResponseDto> debit(@PathVariable Long merchantId,
                                                          @RequestBody BigDecimal amount) {
        AmountRequestDto requestDto = new AmountRequestDto();
        requestDto.setAmount(amount);
        MerchantResponseDto merchant = merchantService.debit(merchantId, requestDto);
        return ResponseEntity.ok(toWalletCompat(merchant));
    }

    @PostMapping("/{merchantId}/credit")
    public ResponseEntity<WalletCompatResponseDto> credit(@PathVariable Long merchantId,
                                                           @RequestBody BigDecimal amount) {
        AmountRequestDto requestDto = new AmountRequestDto();
        requestDto.setAmount(amount);
        MerchantResponseDto merchant = merchantService.credit(merchantId, requestDto);
        return ResponseEntity.ok(toWalletCompat(merchant));
    }

    @PostMapping("/{merchantId}/acquireLock")
    public ResponseEntity<Boolean> acquireLock(@PathVariable Long merchantId,
                                               @RequestParam(defaultValue = "60") long timeoutSeconds) {
        return ResponseEntity.ok(merchantLockService.acquireLock(merchantId));
    }

    @PostMapping("/{merchantId}/releaseLock")
    public ResponseEntity<Void> releaseLock(@PathVariable Long merchantId) {
        merchantLockService.releaseLock(merchantId);
        return ResponseEntity.ok().build();
    }

    private WalletCompatResponseDto toWalletCompat(MerchantResponseDto merchant) {
        WalletCompatResponseDto dto = new WalletCompatResponseDto();
        dto.setId(merchant.getId());
        dto.setUserId(merchant.getUserId());
        dto.setBalance(merchant.getAvailableBalance());
        dto.setCurrency("INR");
        dto.setStatus("ACTIVE".equalsIgnoreCase(merchant.getStatus()) ? "ACTIVE" : "BLOCKED");
        return dto;
    }
}
