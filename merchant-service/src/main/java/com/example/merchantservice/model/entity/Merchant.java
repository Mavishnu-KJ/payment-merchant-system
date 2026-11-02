package com.example.merchantservice.model.entity;

import com.example.merchantservice.model.enums.MerchantStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "merchants",
        indexes = {
                @Index(name = "idx_merchant_email", columnList = "email"),
                @Index(name = "idx_merchant_user_id", columnList = "userId")
        }
)
@Data
public class Merchant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 32)
    private String merchantCode;

    @Column(nullable = false, length = 120)
    private String businessName;

    // Logical link to user-service owner; no JPA relation across services.
    @Column(nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal availableBalance = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal dailyLimit = new BigDecimal("100000.00");

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MerchantStatus status = MerchantStatus.ACTIVE;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    public boolean isActive() {
        return this.status == MerchantStatus.ACTIVE;
    }
}
