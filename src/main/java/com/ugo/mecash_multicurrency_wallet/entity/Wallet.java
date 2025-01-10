package com.ugo.mecash_multicurrency_wallet.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Table(name = "Wallet")
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String currencyCode;

    @Column(nullable = false)
    private BigDecimal balance;

    @Column(nullable = false)
     private Boolean isActive;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "max_transactions_per_day")
    private Integer maxTransactionsPerDay;

    public BigDecimal getMaxBalance() {
        return BigDecimal.valueOf(10000);
    }

    public Integer getMaxTransactionsPerDay() {
        return maxTransactionsPerDay;
    }

    public boolean isActive() {
        return isActive;
    }
}

