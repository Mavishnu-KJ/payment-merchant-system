package com.example.transactionservice.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PayToMerchantRequestDto {

    @NotNull
    private Long merchantId;

    @NotNull
    @Positive
    private BigDecimal amount;

    private String description;
}
