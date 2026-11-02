package com.example.merchantservice.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MerchantResponseDto {
    private Long id;
    private String merchantCode;
    private String businessName;
    private Long userId;
    private String email;
    private BigDecimal availableBalance;
    private BigDecimal dailyLimit;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
