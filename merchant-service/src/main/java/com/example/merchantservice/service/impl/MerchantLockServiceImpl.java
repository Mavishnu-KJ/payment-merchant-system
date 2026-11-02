package com.example.merchantservice.service.impl;

import com.example.merchantservice.service.MerchantLockService;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MerchantLockServiceImpl implements MerchantLockService {

    private final Set<Long> activeLocks = ConcurrentHashMap.newKeySet();

    @Override
    public boolean acquireLock(Long merchantId) {
        return activeLocks.add(merchantId);
    }

    @Override
    public void releaseLock(Long merchantId) {
        activeLocks.remove(merchantId);
    }
}
