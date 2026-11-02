package com.example.merchantservice.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class BalanceResponseDto {
    private Long merchantId;
    private BigDecimal availableBalance;
}
