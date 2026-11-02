package com.example.transactionservice.model.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MerchantResponseDto {
    private Long id;
    private Long userId;
    private BigDecimal availableBalance;
    private String status;
    private String description;
}
