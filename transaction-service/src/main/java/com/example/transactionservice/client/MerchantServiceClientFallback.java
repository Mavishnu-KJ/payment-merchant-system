package com.example.transactionservice.client;

import com.example.transactionservice.model.dto.MerchantResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
public class MerchantServiceClientFallback implements MerchantServiceClient {

    @Override
    public MerchantResponseDto getMerchantById(Long merchantId) {
        log.error("Merchant Service is down. Fallback triggered for getMerchantById({})", merchantId);
        return createEmptyMerchantResponse();
    }

    @Override
    public MerchantResponseDto credit(Long merchantId, BigDecimal amount) {
        log.error("Merchant Service credit failed. Fallback triggered for merchantId: {}, amount: {}", merchantId, amount);
        return createEmptyMerchantResponse();
    }

    private MerchantResponseDto createEmptyMerchantResponse() {
        MerchantResponseDto merchantResponseDto = new MerchantResponseDto();
        merchantResponseDto.setAvailableBalance(BigDecimal.ZERO);
        merchantResponseDto.setDescription("Service temporarily unavailable");
        return merchantResponseDto;
    }
}
