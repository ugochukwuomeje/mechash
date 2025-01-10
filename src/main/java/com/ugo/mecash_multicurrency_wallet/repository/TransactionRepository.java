package com.ugo.mecash_multicurrency_wallet.repository;

import com.ugo.mecash_multicurrency_wallet.entity.Transaction;
import com.ugo.mecash_multicurrency_wallet.entity.Wallet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByWallet(Wallet wallet);

    @Query("SELECT t FROM Transaction t WHERE t.wallet.user.id = :userId " +
            "AND t.transactionDate BETWEEN :startDate AND :endDate")
    Page<Transaction> findByWalletIdAndDateRange(@Param("userId") Long userId,
                                                 @Param("startDate") String startDate,
                                                 @Param("endDate") String endDate,
                                                 Pageable pageable);
}


