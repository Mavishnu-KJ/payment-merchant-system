package com.example.merchantservice.model.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class WalletCompatResponseDto {
    private Long id;
    private Long userId;
    private BigDecimal balance;
    private String currency;
    private String status;
    private String description;
}
