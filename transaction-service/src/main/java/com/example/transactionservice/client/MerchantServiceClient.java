package com.example.transactionservice.client;

import com.example.transactionservice.model.dto.MerchantResponseDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;

@FeignClient(name = "merchant-service", fallback = MerchantServiceClientFallback.class)
public interface MerchantServiceClient {

    @GetMapping("/api/merchants/internal/{merchantId}")
    @CircuitBreaker(name = "merchantServiceCircuit")
    MerchantResponseDto getMerchantById(@PathVariable Long merchantId);

    @PostMapping("/api/merchants/internal/{merchantId}/credit")
    @CircuitBreaker(name = "merchantServiceCircuit")
    MerchantResponseDto credit(@PathVariable Long merchantId, @RequestBody BigDecimal amount);
}
