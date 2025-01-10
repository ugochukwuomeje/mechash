package com.ugo.mecash_multicurrency_wallet.repository;

import com.ugo.mecash_multicurrency_wallet.entity.Wallet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
    Optional<Wallet> findByWalletId(Long userId);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.wallet.id = :walletId AND DATE(t.transactionDate) = CURRENT_DATE")
    Integer countTransactionsForToday(@Param("walletId") Long walletId);



    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Wallet w WHERE w.id = :userId AND w.currencyCode = :currencyCode")
    Optional<Wallet> findByIdWithLock(@Param("userId") Long userId, @Param("currencyCode") String currencyCode);

    @Query("SELECT w FROM Wallet w WHERE w.user.id = :userId AND w.currencyCode = :currencyCode")
    Optional<Wallet> findByIdUserIdAndCurrencyCode(@Param("userId") Long userId, @Param("currencyCode") String currencyCode);

}

