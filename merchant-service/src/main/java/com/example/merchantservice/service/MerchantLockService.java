package com.example.merchantservice.service;

public interface MerchantLockService {
    boolean acquireLock(Long merchantId);
    void releaseLock(Long merchantId);
}
