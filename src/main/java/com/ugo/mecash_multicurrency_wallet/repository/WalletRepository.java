package com.ugo.mecash_multicurrency_wallet.repository;

import com.ugo.mecash_multicurrency_wallet.entity.Transaction;
import com.ugo.mecash_multicurrency_wallet.entity.User;
import com.ugo.mecash_multicurrency_wallet.entity.Wallet;
import jakarta.persistence.LockModeType;
import org.hibernate.query.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
//    @Query("SELECT w FROM Wallet w WHERE w.walletId = :walletId")
//    Optional<Wallet> findByWalletId(@Param("walletId") Long walletId);


    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.wallet.id = :walletId AND DATE(t.transactionDate) = CURRENT_DATE")
    Integer countTransactionsForToday(@Param("walletId") Long walletId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Wallet w WHERE w.id = :userId AND w.currencyCode = :currencyCode")
    Optional<Wallet> findByIdWithLock(@Param("userId") Long userId, @Param("currencyCode") String currencyCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Wallet w WHERE w.id = :userId AND w.wallet = :wallet AND w.currencyCode = :currencyCode")
    Optional<Wallet> findByIdWithLock(@Param("userId") Long userId, @Param("wallet")Long wallet, @Param("currencyCode") String currencyCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT wallet, balance FROM Wallet w WHERE User = ?1")
    Optional<List<Map<String, Object>>> findByIdWithLock(User user);


    @Query("UPDATE Wallet set balance =  ?1 where wallet = ?2")
    @Modifying
    void updateWallet(BigDecimal amount, Long wallet);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("from Wallet where wallet = ?1")
    Optional<Wallet> findByWallet(Long wallet);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Wallet w WHERE w.id = :userId AND w.currencyCode = :currencyCode")
    Optional<Wallet> findRecipientByIdWithLock(@Param("userId") Long userId, @Param("currencyCode") String currencyCode);

    @Query("SELECT w FROM Wallet w WHERE w.user.id = :userId AND w.currencyCode = :currencyCode")
    Optional<Wallet> findByIdUserIdAndCurrencyCode(@Param("userId") Long userId, @Param("currencyCode") String currencyCode);

    @Query("UPDATE Wallet SET isActive = ?1 WHERE wallet = ?2 AND user = ?3")
    @Modifying
    void blockAccount(boolean account_status, long wallet, User user);

    @Query("UPDATE Wallet SET pin_attempt = ?1 WHERE wallet = ?2 AND user = ?3")
    @Modifying
    void incrementPinAttempt(int pin_attempt, Long wallet, User user);

}

