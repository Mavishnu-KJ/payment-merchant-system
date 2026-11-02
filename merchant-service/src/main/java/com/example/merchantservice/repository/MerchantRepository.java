package com.example.merchantservice.repository;

import com.example.merchantservice.model.entity.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MerchantRepository extends JpaRepository<Merchant, Long> {
    Optional<Merchant> findByEmail(String email);
    Optional<Merchant> findByMerchantCode(String merchantCode);
    Optional<Merchant> findByUserId(Long userId);
}
