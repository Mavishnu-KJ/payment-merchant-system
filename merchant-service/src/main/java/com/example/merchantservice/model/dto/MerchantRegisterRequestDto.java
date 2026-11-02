package com.example.merchantservice.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MerchantRegisterRequestDto {

    @NotBlank(message = "merchantCode is required")
    private String merchantCode;

    @NotBlank(message = "businessName is required")
    private String businessName;

    @NotNull(message = "userId is required")
    private Long userId;

    @Email(message = "email must be valid")
    @NotBlank(message = "email is required")
    private String email;
}
