package com.example.merchantservice.service.impl;

import com.example.merchantservice.exceptions.InsufficientBalanceException;
import com.example.merchantservice.exceptions.ResourceConflictException;
import com.example.merchantservice.model.dto.AmountRequestDto;
import com.example.merchantservice.model.dto.BalanceResponseDto;
import com.example.merchantservice.model.dto.MerchantRegisterRequestDto;
import com.example.merchantservice.model.dto.MerchantResponseDto;
import com.example.merchantservice.model.entity.Merchant;
import com.example.merchantservice.model.enums.MerchantStatus;
import com.example.merchantservice.repository.MerchantRepository;
import com.example.merchantservice.service.MerchantService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class MerchantServiceImpl implements MerchantService {

    private final MerchantRepository merchantRepository;

    @Override
    @Transactional
    public MerchantResponseDto register(MerchantRegisterRequestDto requestDto) {
        merchantRepository.findByEmail(requestDto.getEmail())
                .ifPresent(existing -> {
                    throw new ResourceConflictException("Merchant already exists for email: " + requestDto.getEmail());
                });

        merchantRepository.findByMerchantCode(requestDto.getMerchantCode())
                .ifPresent(existing -> {
                    throw new ResourceConflictException("Merchant already exists for code: " + requestDto.getMerchantCode());
                });

        Merchant merchant = new Merchant();
        merchant.setMerchantCode(requestDto.getMerchantCode());
        merchant.setBusinessName(requestDto.getBusinessName());
        merchant.setUserId(requestDto.getUserId());
        merchant.setEmail(requestDto.getEmail());

        Merchant savedMerchant = merchantRepository.save(merchant);
        return toResponse(savedMerchant);
    }

    @Override
    public MerchantResponseDto getById(Long merchantId) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new EntityNotFoundException("Merchant not found for id: " + merchantId));
        return toResponse(merchant);
    }

    @Override
    public MerchantResponseDto getByUserId(Long userId) {
        Merchant merchant = merchantRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Merchant not found for userId: " + userId));
        return toResponse(merchant);
    }

    @Override
    @Transactional
    public MerchantResponseDto credit(Long merchantId, AmountRequestDto requestDto) {
        Merchant merchant = getActiveMerchant(merchantId);
        validateAmount(requestDto.getAmount());

        merchant.setAvailableBalance(merchant.getAvailableBalance().add(requestDto.getAmount()));
        Merchant savedMerchant = merchantRepository.save(merchant);
        return toResponse(savedMerchant);
    }

    @Override
    @Transactional
    public MerchantResponseDto debit(Long merchantId, AmountRequestDto requestDto) {
        Merchant merchant = getActiveMerchant(merchantId);
        validateAmount(requestDto.getAmount());

        if (merchant.getAvailableBalance().compareTo(requestDto.getAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient merchant balance for debit operation");
        }

        merchant.setAvailableBalance(merchant.getAvailableBalance().subtract(requestDto.getAmount()));
        Merchant savedMerchant = merchantRepository.save(merchant);
        return toResponse(savedMerchant);
    }

    @Override
    public BalanceResponseDto getBalance(Long merchantId) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new EntityNotFoundException("Merchant not found for id: " + merchantId));
        return new BalanceResponseDto(merchant.getId(), merchant.getAvailableBalance());
    }

    private Merchant getActiveMerchant(Long merchantId) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new EntityNotFoundException("Merchant not found for id: " + merchantId));

        if (merchant.getStatus() != MerchantStatus.ACTIVE) {
            throw new IllegalStateException("Merchant is not active");
        }

        return merchant;
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
    }

    private MerchantResponseDto toResponse(Merchant merchant) {
        MerchantResponseDto dto = new MerchantResponseDto();
        dto.setId(merchant.getId());
        dto.setMerchantCode(merchant.getMerchantCode());
        dto.setBusinessName(merchant.getBusinessName());
        dto.setUserId(merchant.getUserId());
        dto.setEmail(merchant.getEmail());
        dto.setAvailableBalance(merchant.getAvailableBalance());
        dto.setDailyLimit(merchant.getDailyLimit());
        dto.setStatus(merchant.getStatus().name());
        dto.setCreatedAt(merchant.getCreatedAt());
        dto.setUpdatedAt(merchant.getUpdatedAt());
        return dto;
    }
}
