package com.example.merchantservice.service;

import com.example.merchantservice.model.dto.BalanceResponseDto;
import com.example.merchantservice.model.dto.AmountRequestDto;
import com.example.merchantservice.model.dto.MerchantRegisterRequestDto;
import com.example.merchantservice.model.dto.MerchantResponseDto;

public interface MerchantService {
    MerchantResponseDto register(MerchantRegisterRequestDto requestDto);
    MerchantResponseDto getById(Long merchantId);
    MerchantResponseDto getByUserId(Long userId);
    MerchantResponseDto credit(Long merchantId, AmountRequestDto requestDto);
    MerchantResponseDto debit(Long merchantId, AmountRequestDto requestDto);
    BalanceResponseDto getBalance(Long merchantId);
}
